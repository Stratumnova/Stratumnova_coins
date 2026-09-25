package org.stratumnova.buildbycoin.world
import android.graphics.Bitmap
import android.graphics.Color
import java.io.File
data class MapTile(var color:Int,var kind:SpaceKind)
data class MapDraft(val width:Int,val height:Int,val tiles:MutableList<MapTile>,val source:File?=null){fun tile(x:Int,y:Int)=tiles[y*width+x]}
object MapImporter{
 private val palette=listOf(
  Color.rgb(210,55,115) to SpaceKind.PATH,
  Color.rgb(115,145,55) to SpaceKind.FLOOR,
  Color.rgb(145,85,50) to SpaceKind.OBJECT,
  Color.rgb(55,125,190) to SpaceKind.WATER,
  Color.rgb(235,235,225) to SpaceKind.WALL)
 private fun dist(a:Int,b:Int):Int{val dr=Color.red(a)-Color.red(b);val dg=Color.green(a)-Color.green(b);val db=Color.blue(a)-Color.blue(b);return dr*dr+dg*dg+db*db}
 private fun classify(c:Int)=palette.minByOrNull{dist(c,it.first)}?.second?:SpaceKind.FLOOR
 fun fromBitmap(bitmap:Bitmap,columns:Int,rows:Int,source:File?=null):MapDraft{
  val out=mutableListOf<MapTile>();for(y in 0 until rows)for(x in 0 until columns){val px=((x+.5f)*bitmap.width/columns).toInt().coerceIn(0,bitmap.width-1);val py=((y+.5f)*bitmap.height/rows).toInt().coerceIn(0,bitmap.height-1);val c=bitmap.getPixel(px,py);out+=MapTile(c,classify(c))}
  return MapDraft(columns,rows,out,source)
 }
 fun commit(d:MapDraft,store:WorldStore){
  val cellsX=(d.width+8)/9;val cellsY=(d.height+8)/9;store.replaceFromDraft(cellsX,cellsY){wx,wy,x,y->val gx=wx*9+x;val gy=wy*9+y;if(gx<d.width&&gy<d.height)d.tile(gx,gy).kind else SpaceKind.WALL}
 }
}