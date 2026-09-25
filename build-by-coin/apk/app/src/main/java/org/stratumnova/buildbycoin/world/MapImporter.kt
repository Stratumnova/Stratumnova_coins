package org.stratumnova.buildbycoin.world
import android.graphics.Bitmap
import android.graphics.Color
import java.io.File
data class MapLegendEntry(var color:Int,var kind:SpaceKind)
data class MapTile(var color:Int,var kind:SpaceKind)
data class MapDraft(val width:Int,val height:Int,val tiles:MutableList<MapTile>,val source:File?=null){fun tile(x:Int,y:Int)=tiles[y*width+x]}
object MapImporter{
 fun defaultLegend()=mutableListOf(
  MapLegendEntry(Color.rgb(210,55,115),SpaceKind.PATH),
  MapLegendEntry(Color.rgb(115,145,55),SpaceKind.FLOOR),
  MapLegendEntry(Color.rgb(145,85,50),SpaceKind.OBJECT),
  MapLegendEntry(Color.rgb(55,125,190),SpaceKind.WATER),
  MapLegendEntry(Color.rgb(235,235,225),SpaceKind.WALL))
 private fun dist(a:Int,b:Int):Int{val dr=Color.red(a)-Color.red(b);val dg=Color.green(a)-Color.green(b);val db=Color.blue(a)-Color.blue(b);return dr*dr+dg*dg+db*db}
 private fun classify(c:Int,legend:List<MapLegendEntry>)=legend.minByOrNull{dist(c,it.color)}?.kind?:SpaceKind.FLOOR
 fun fromBitmap(bitmap:Bitmap,columns:Int,rows:Int,legend:List<MapLegendEntry> = defaultLegend(),cropLeft:Float=0f,cropTop:Float=0f,cropRight:Float=1f,cropBottom:Float=1f,source:File?=null):MapDraft{
  val left=(bitmap.width*cropLeft.coerceIn(0f,.95f)).toInt();val top=(bitmap.height*cropTop.coerceIn(0f,.95f)).toInt()
  val right=(bitmap.width*cropRight.coerceIn(.05f,1f)).toInt().coerceAtLeast(left+1);val bottom=(bitmap.height*cropBottom.coerceIn(.05f,1f)).toInt().coerceAtLeast(top+1)
  val out=mutableListOf<MapTile>();for(y in 0 until rows)for(x in 0 until columns){val px=(left+(x+.5f)*(right-left)/columns).toInt().coerceIn(left,right-1);val py=(top+(y+.5f)*(bottom-top)/rows).toInt().coerceIn(top,bottom-1);val color=bitmap.getPixel(px,py);out+=MapTile(color,classify(color,legend))}
  return MapDraft(columns,rows,out,source)
 }
 fun commit(d:MapDraft,store:WorldStore){val cellsX=(d.width+8)/9;val cellsY=(d.height+8)/9;store.replaceFromDraft(cellsX,cellsY){wx,wy,x,y->val gx=wx*9+x;val gy=wy*9+y;if(gx<d.width&&gy<d.height)d.tile(gx,gy).kind else SpaceKind.WALL}}
}