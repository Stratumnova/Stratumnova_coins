package org.stratumnova.buildbycoin.runtime

enum class TileKind { FLOOR, BLOCKED, PATH, WATER, GRASS, DIRT, SAND }
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

    // The room is a universal test substrate. Presets mutate tiles; they do not create a separate runtime.
    fun applyPreset(name:String){
        when(name.uppercase()){
            "MARKET" -> { resetBaseRoom(); applyMarketFixture() }
            "FISHING" -> { resetBaseRoom(); addPond(2,12,9,20); paintRect(10,12,10,20,TileKind.PATH); paintRect(11,20,12,23,TileKind.PATH) }
            "COMBAT" -> {
                resetBaseRoom()
                paintRect(4,4,20,20,TileKind.FLOOR)
                paintRect(7,7,8,8,TileKind.BLOCKED)
                paintRect(16,7,17,8,TileKind.BLOCKED)
                paintRect(11,15,13,16,TileKind.BLOCKED)
                paintRect(12,21,12,23,TileKind.PATH)
            }
            "FARMING" -> { resetBaseRoom(); paintRect(3,4,20,18,TileKind.DIRT); paintRect(4,5,9,17,TileKind.GRASS); paintRect(14,5,19,17,TileKind.GRASS); paintRect(11,4,12,23,TileKind.PATH) }
            "ECOLOGY" -> { resetBaseRoom(); paintRect(2,3,22,21,TileKind.GRASS); addPond(3,5,8,10); paintRect(12,3,13,22,TileKind.PATH) }
            "NPC" -> { resetBaseRoom(); paintRect(4,5,20,6,TileKind.PATH); paintRect(4,12,20,13,TileKind.PATH); paintRect(4,19,20,20,TileKind.PATH); add("NPC-1",6,5,"npc"); add("NPC-2",12,12,"npc"); add("NPC-3",18,19,"npc") }
            "RESOURCE" -> { resetBaseRoom(); paintRect(3,4,8,9,TileKind.DIRT); paintRect(16,4,21,9,TileKind.GRASS); paintRect(3,15,8,20,TileKind.SAND); addPond(16,15,21,20); paintRect(11,3,13,23,TileKind.PATH) }
            "PUZZLE" -> { resetBaseRoom(); paintRect(4,4,20,20,TileKind.PATH); paintRect(6,6,18,18,TileKind.FLOOR); paintRect(9,6,10,15,TileKind.BLOCKED); paintRect(14,9,15,18,TileKind.BLOCKED) }
            "RACING" -> { resetBaseRoom(); paintRect(3,3,21,21,TileKind.PATH); paintRect(6,6,18,18,TileKind.FLOOR); paintRect(11,18,13,23,TileKind.PATH) }
            "SHOOTING" -> { resetBaseRoom(); paintRect(3,3,21,20,TileKind.FLOOR); paintRect(5,5,5,19,TileKind.BLOCKED); paintRect(19,5,19,19,TileKind.BLOCKED); add("TARGET-1",8,5,"target"); add("TARGET-2",12,5,"target"); add("TARGET-3",16,5,"target") }
            "PLATFORMER" -> { resetBaseRoom(); paintRect(2,20,22,20,TileKind.PATH); paintRect(5,16,9,16,TileKind.BLOCKED); paintRect(12,12,16,12,TileKind.BLOCKED); paintRect(18,8,21,8,TileKind.BLOCKED) }
            "TOPDOWN" -> { resetBaseRoom(); paintRect(4,4,20,20,TileKind.FLOOR); paintRect(8,8,16,8,TileKind.BLOCKED); paintRect(8,16,16,16,TileKind.BLOCKED); paintRect(12,9,12,15,TileKind.PATH) }
            "VOCABULARY" -> { resetBaseRoom(); paintRect(5,5,19,19,TileKind.PATH); add("PROMPT",12,7,"prompt"); add("ANSWER-A",8,15,"answer"); add("ANSWER-B",12,15,"answer"); add("ANSWER-C",16,15,"answer") }
            "DATA" -> resetBaseRoom()
            "SPRITE" -> { resetBaseRoom(); add("SPRITE-TEST",12,12,"sprite-test") }
            "WEATHER" -> { resetBaseRoom(); paintRect(2,2,22,22,TileKind.GRASS); paintRect(12,3,12,23,TileKind.PATH) }
            "SANDBOX" -> resetBaseRoom()
        }
    }
}
