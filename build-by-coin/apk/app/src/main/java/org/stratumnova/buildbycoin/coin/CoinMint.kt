package org.stratumnova.buildbycoin.coin
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

data class CandidateFields(
 val coinId:String="UNRESOLVED", val type:String="UNRESOLVED",
 val domain:String="UNRESOLVED", val role:String="UNRESOLVED",
 val category:String="UNRESOLVED", val summary:String="UNRESOLVED"
)

object CoinMint {
 fun candidate(artifact:File,fields:CandidateFields=CandidateFields()):JSONObject {
  val sha=sha256(artifact)
  return JSONObject().apply{
   put("schema","a25.candidate.coin.v0.1");put("status","CANDIDATE")
   put("coin_id",fields.coinId)
   put("current",JSONObject().put("name",artifact.name).put("sha256",sha).put("bytes",artifact.length()))
   put("classification",JSONObject().put("type",fields.type).put("domain",fields.domain).put("role",fields.role).put("category",fields.category))
   put("summary",fields.summary)
   put("build_by_coin",JSONObject().put("provides","UNRESOLVED").put("requires","UNRESOLVED").put("compatible_with","UNRESOLVED").put("conflicts_with","UNRESOLVED").put("tunable","UNRESOLVED"))
   put("public_audit",JSONObject().put("source_preserved",true).put("auto_claims",false))
  }
 }
 private fun sha256(f:File):String{val m=MessageDigest.getInstance("SHA-256");f.inputStream().use{i->val b=ByteArray(65536);while(true){val n=i.read(b);if(n<0)break;m.update(b,0,n)}};return m.digest().joinToString(""){"%02x".format(it)}}
}