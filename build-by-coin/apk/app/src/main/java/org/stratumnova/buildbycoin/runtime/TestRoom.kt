package org.stratumnova.buildbycoin.runtime

enum class TileKind { FLOOR, BLOCKED, PATH, WATER, GRASS, DIRT, SAND, TRACK, PLATFORM, GOAL, HAZARD, CROP, INTERACT }
data class TestTile(
    val x:Int,
    val y:Int,
    var kind:TileKind=TileKind.FLOOR,
    var occupiedBy:String?=null,
    var assetRef:String?=null
){
    val spaceCode:String get()="S-X%02d-Y%02d".format(x,y)
    val walkable:Boolean get()=kind!=TileKind.BLOCKED && kind!=TileKind.WATER
}
data class RoomEntity(val id:String,var x:Int,var y:Int,val visualId:String,var assetRef:String?=null)

class TestRoom(val width:Int=25,val height:Int=25){
    val tiles=Array(height){y->Array(width){x->TestTile(x,y)}}
    val entities=linkedMapOf<String,RoomEntity>()

    init{ resetBaseRoom(); applyMarketFixture() }

    fun resetBaseRoom(){
        entities.clear()
        for(y in 0 until height)for(x in 0 until width){
            val t=tiles[y][x]
            val edge=x==0||y==0||x==width-1||y==height-1
            t.kind=if(edge) TileKind.BLOCKED else TileKind.FLOOR
            t.occupiedBy=null
            t.assetRef=null
        }
        // One neutral entrance exists in every fixture. Everything else belongs to the selected fixture.
        tiles[24][12].kind=TileKind.PATH
    }

    private fun applyMarketFixture(){
        tiles[23][12].kind=TileKind.PATH
        for(y in 8..23) tiles[y][12].kind=TileKind.PATH
        for(x in 5..19) tiles[8][x].kind=TileKind.PATH
        add("STALL-1",5,5,"stall-1");add("STALL-2",12,5,"stall-2");add("STALL-3",19,5,"stall-3")
        add("VENDOR-1",5,7,"vendor-1");add("VENDOR-2",12,7,"vendor-2");add("VENDOR-3",19,7,"vendor-3")
        add("CUSTOMER-1",11,22,"customer-1");add("CUSTOMER-2",12,22,"customer-2");add("CUSTOMER-3",13,22,"customer-3")
    }

    fun addEntity(id:String,x:Int,y:Int,visual:String){ add(id,x,y,visual) }

    private fun add(id:String,x:Int,y:Int,visual:String){
        entities[id]=RoomEntity(id,x,y,visual)
        tiles[y][x].occupiedBy=id
    }

    fun tile(x:Int,y:Int):TestTile?=if(x in 0 until width && y in 0 until height)tiles[y][x] else null
    fun tile(code:String):TestTile?{
        val m=Regex("""S-X(\d{2})-Y(\d{2})""").matchEntire(code)?:return null
        return tile(m.groupValues[1].toInt(),m.groupValues[2].toInt())
    }
    fun setTile(code:String,kind:TileKind,assetRef:String?=null):Boolean{
        val t=tile(code)?:return false;t.kind=kind;t.assetRef=assetRef;return true
    }
    fun paintRect(x0:Int,y0:Int,x1:Int,y1:Int,kind:TileKind,assetRef:String?=null){
        for(y in minOf(y0,y1)..maxOf(y0,y1))for(x in minOf(x0,x1)..maxOf(x0,x1))tile(x,y)?.let{it.kind=kind;it.assetRef=assetRef}
    }
    fun addPond(x0:Int,y0:Int,x1:Int,y1:Int,assetRef:String?=null)=paintRect(x0,y0,x1,y1,TileKind.WATER,assetRef)
    private fun track(x:Int,y:Int){ tile(x,y)?.kind=TileKind.TRACK }
    private fun ovalTrack(){
        for(x in 6..18){track(x,4);track(x,20)}
        for(y in 7..17){track(3,y);track(21,y)}
        listOf(4 to 5,5 to 4,19 to 4,20 to 5,4 to 19,5 to 20,19 to 20,20 to 19,3 to 6,21 to 6,3 to 18,21 to 18).forEach{track(it.first,it.second)}
        paintRect(11,20,13,23,TileKind.TRACK);tile(12,20)?.kind=TileKind.GOAL
    }
    private fun figureEightTrack(){
        for(i in 0..7){track(4+i,5+i);track(20-i,5+i);track(4+i,19-i);track(20-i,19-i)}
        paintRect(10,11,14,13,TileKind.TRACK);paintRect(11,20,13,23,TileKind.TRACK);tile(12,20)?.kind=TileKind.GOAL
    }
    private fun platformCourse(){
        paintRect(2,21,22,22,TileKind.PLATFORM)
        paintRect(4,17,8,18,TileKind.PLATFORM);paintRect(11,14,15,15,TileKind.PLATFORM);paintRect(18,10,21,11,TileKind.PLATFORM)
        paintRect(14,20,16,20,TileKind.HAZARD);tile(20,9)?.kind=TileKind.GOAL
        add("JUMP-TARGET-1",6,16,"jump-target");add("JUMP-TARGET-2",13,13,"jump-target");add("JUMP-TARGET-3",19,9,"jump-target")
    }

    // The room is a universal test substrate. Presets mutate tiles; they do not create a separate runtime.
    fun applyPreset(name:String){
        when(name.uppercase()){
            "MARKET" -> { resetBaseRoom(); applyMarketFixture() }
            "FISHING" -> { resetBaseRoom(); addPond(2,7,14,19); paintRect(15,7,16,19,TileKind.PATH); paintRect(11,20,12,23,TileKind.PATH); add("FISH-SPAWN-A",6,11,"fish"); add("FISH-SPAWN-B",10,15,"fish"); add("CAST-POINT",15,13,"cast-point") }
            "COMBAT" -> {
                resetBaseRoom()
                paintRect(4,4,20,20,TileKind.FLOOR)
                paintRect(7,7,8,8,TileKind.BLOCKED)
                paintRect(16,7,17,8,TileKind.BLOCKED)
                paintRect(11,15,13,16,TileKind.BLOCKED)
                paintRect(12,21,12,23,TileKind.PATH)
            }
            "FARMING" -> { resetBaseRoom(); paintRect(3,4,20,18,TileKind.DIRT); paintRect(4,5,9,17,TileKind.CROP); paintRect(14,5,19,17,TileKind.CROP); paintRect(11,4,12,23,TileKind.PATH); add("TOOL-SHED",12,5,"shed"); add("HARVEST-BIN",12,18,"bin") }
            "ECOLOGY" -> { resetBaseRoom(); paintRect(2,3,22,21,TileKind.GRASS); addPond(3,5,8,10); paintRect(12,3,13,22,TileKind.PATH) }
            "NPC" -> { resetBaseRoom(); paintRect(4,5,20,6,TileKind.PATH); paintRect(4,12,20,13,TileKind.PATH); paintRect(4,19,20,20,TileKind.PATH); add("NPC-1",6,5,"npc"); add("NPC-2",12,12,"npc"); add("NPC-3",18,19,"npc") }
            "RESOURCE" -> { resetBaseRoom(); paintRect(3,4,8,9,TileKind.DIRT); paintRect(16,4,21,9,TileKind.GRASS); paintRect(3,15,8,20,TileKind.SAND); addPond(16,15,21,20); paintRect(11,3,13,23,TileKind.PATH) }
            "PUZZLE" -> { resetBaseRoom(); paintRect(4,4,20,20,TileKind.PATH); paintRect(6,6,18,18,TileKind.FLOOR); paintRect(9,6,10,15,TileKind.BLOCKED); paintRect(14,9,15,18,TileKind.BLOCKED) }
            "RACING" -> { resetBaseRoom(); ovalTrack(); add("RACER-1",12,19,"racer"); add("CHECKPOINT-A",3,12,"checkpoint"); add("CHECKPOINT-B",21,12,"checkpoint") }
            "RACING-8" -> { resetBaseRoom(); figureEightTrack(); add("RACER-1",12,19,"racer"); add("CROSSOVER",12,12,"checkpoint") }
            "SHOOTING" -> { resetBaseRoom(); paintRect(3,3,21,20,TileKind.FLOOR); paintRect(5,5,5,19,TileKind.BLOCKED); paintRect(19,5,19,19,TileKind.BLOCKED); add("TARGET-1",8,5,"target"); add("TARGET-2",12,5,"target"); add("TARGET-3",16,5,"target") }
            "PLATFORMER" -> { resetBaseRoom(); platformCourse() }
            "PLATFORMER-L" -> { resetBaseRoom(); platformCourse(); add("FORCE-DIR",21,20,"force-left") }
            "PLATFORMER-R" -> { resetBaseRoom(); platformCourse(); add("FORCE-DIR",3,20,"force-right") }
            "TOPDOWN" -> { resetBaseRoom(); paintRect(4,4,20,20,TileKind.FLOOR); paintRect(8,8,16,8,TileKind.BLOCKED); paintRect(8,16,16,16,TileKind.BLOCKED); paintRect(12,9,12,15,TileKind.PATH) }
            "VOCABULARY" -> { resetBaseRoom(); paintRect(5,5,19,19,TileKind.PATH); add("PROMPT",12,7,"prompt"); add("ANSWER-A",8,15,"answer"); add("ANSWER-B",12,15,"answer"); add("ANSWER-C",16,15,"answer") }
            "DATA" -> resetBaseRoom()
            "SPRITE" -> { resetBaseRoom(); add("SPRITE-TEST",12,12,"sprite-test") }
            "WEATHER" -> { resetBaseRoom(); paintRect(2,2,22,22,TileKind.GRASS); paintRect(12,3,12,23,TileKind.PATH) }
            "SANDBOX" -> resetBaseRoom()
        }
    }
}
