package org.stratumnova.buildbycoin.world

import android.graphics.Bitmap
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

object MapArchive {
 fun preserve(root:File, bitmap:Bitmap, draft:MapDraft, legend:List<MapLegendEntry>, crop:List<Float>):File {
  val dir=File(root,"world/map-imports/"+System.currentTimeMillis()).apply{mkdirs()}
  val source=File(dir,"source.png")
  source.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
  val tiles=JSONArray()
  for(y in 0 until draft.height)for(x in 0 until draft.width){val t=draft.tile(x,y);tiles.put(JSONObject().put("x",x).put("y",y).put("source_color",String.format("#%06X",0xFFFFFF and t.color)).put("kind",t.kind.name))}
  val mappings=JSONArray();legend.forEach{mappings.put(JSONObject().put("color",String.format("#%06X",0xFFFFFF and it.color)).put("kind",it.kind.name))}
  val manifest=JSONObject().put("schema","stratumnova.map-import.v0.1").put("source","source.png").put("source_sha256",sha(source)).put("columns",draft.width).put("rows",draft.height).put("crop_percent",JSONArray(crop.map{it*100f})).put("legend",mappings).put("tiles",tiles)
  File(dir,"coded-map.json").writeText(manifest.toString(2))
  return dir
 }
 private fun sha(file:File):String{val md=MessageDigest.getInstance("SHA-256");file.inputStream().use{input->val b=ByteArray(8192);while(true){val n=input.read(b);if(n<=0)break;md.update(b,0,n)}};return md.digest().joinToString(""){"%02x".format(it)}}
}
