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

    init{
        for(y in 0 until height)for(x in 0 until width){
            val edge=x==0||y==0||x==width-1||y==height-1
            tiles[y][x].kind=if(edge) TileKind.BLOCKED else TileKind.FLOOR
        }
        // Main aisle and branches. Entrance at south edge is explicitly open.
        tiles[23][12].kind=TileKind.PATH
        tiles[24][12].kind=TileKind.PATH
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
    // Test-room mutation example: turn any tile rectangle into a pond for fishing tests.
    fun addPond(x0:Int,y0:Int,x1:Int,y1:Int,assetRef:String?=null)=paintRect(x0,y0,x1,y1,TileKind.WATER,assetRef)
}
