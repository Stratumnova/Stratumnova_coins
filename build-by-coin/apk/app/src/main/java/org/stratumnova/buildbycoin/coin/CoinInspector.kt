package org.stratumnova.buildbycoin.coin
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class CoinSummary(
 val coinId:String,val name:String,val category:String,val role:String,val domain:String,
 val sha256:String,val bytes:Long,val provides:String,val requires:String
)
object CoinInspector{
 private fun field(o:JSONObject?,key:String):String{
  if(o==null||!o.has(key)||o.isNull(key))return "UNRESOLVED"
  val v=o.opt(key)
  return when(v){
   is JSONArray -> if(v.length()==0)"UNRESOLVED" else (0 until v.length()).joinToString(", "){v.opt(it)?.toString()?:"UNRESOLVED"}
   is JSONObject -> if(v.length()==0)"UNRESOLVED" else v.toString()
   else -> v?.toString()?.takeIf{it.isNotBlank()&&it!="unknown"}?:"UNRESOLVED"
  }
 }
 fun inspect(file:File):CoinSummary?{
  if(!file.name.endsWith(".coin.json"))return null
  return try{
   val j=JSONObject(file.readText());val cur=j.optJSONObject("current");val c=j.optJSONObject("classification");val b=j.optJSONObject("build_by_coin")
   CoinSummary(j.optString("coin_id","UNRESOLVED"),cur?.optString("name","UNRESOLVED")?:"UNRESOLVED",c?.optString("category","UNRESOLVED")?:"UNRESOLVED",c?.optString("role","UNRESOLVED")?:"UNRESOLVED",c?.optString("domain","UNRESOLVED")?:"UNRESOLVED",cur?.optString("sha256","UNRESOLVED")?:"UNRESOLVED",cur?.optLong("bytes",-1)?:-1,field(b,"provides"),field(b,"requires"))
  }catch(_:Exception){null}
 }
}