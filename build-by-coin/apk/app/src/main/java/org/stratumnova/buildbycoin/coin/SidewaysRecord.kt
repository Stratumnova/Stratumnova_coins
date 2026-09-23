package org.stratumnova.buildbycoin.coin
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

enum class SidewaysType { VERSION, ADAPTER, CONFIG, ASSET, DOCUMENTATION, EVIDENCE, REPORT, COMPATIBILITY, DEPENDENCY, OTHER }

object SidewaysRecord {
 fun create(masterCoinId:String,file:File,type:SidewaysType,note:String=""):JSONObject =
  JSONObject().put("schema","a25.sideways.record.v0.1").put("master_coin_id",masterCoinId)
   .put("sideways_type",type.name).put("name",file.name).put("bytes",file.length())
   .put("sha256",hash(file)).put("note",note.ifBlank{"UNRESOLVED"})
 private fun hash(f:File):String{val m=MessageDigest.getInstance("SHA-256");f.inputStream().use{i->val b=ByteArray(65536);while(true){val n=i.read(b);if(n<0)break;m.update(b,0,n)}};return m.digest().joinToString(""){"%02x".format(it)}}
}