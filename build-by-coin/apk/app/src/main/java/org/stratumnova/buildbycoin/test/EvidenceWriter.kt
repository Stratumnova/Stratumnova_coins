package org.stratumnova.buildbycoin.test
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
object EvidenceWriter{
 fun write(root:File,run:TestRun):File{root.mkdirs();val dir=File(root,"run_"+System.currentTimeMillis());dir.mkdirs()
  val working=File(root.parentFile,"working")
  val coins=JSONArray()
  if(working.exists())working.walkTopDown().filter{it.isFile&&it.name.endsWith(".coin.json")}.forEach{coin->
   try{
    val j=JSONObject(coin.readText());val id=j.optString("coin_id","UNRESOLVED")
    val cur=j.optJSONObject("current");val baselineSha=cur?.optString("sha256","UNRESOLVED")?:"UNRESOLVED"
    val side=File(coin.parentFile,"sideways")
    val versions=side.listFiles()?.filter{it.isFile&&it.name.endsWith(".sideways.json")}.orEmpty().mapNotNull{r->
     try{JSONObject(r.readText()).takeIf{it.optString("sideways_type")=="VERSION"&&it.optString("version_id","UNRESOLVED")!="UNRESOLVED"}}catch(_:Exception){null}
    }
    if(versions.isEmpty())coins.put(JSONObject().put("coin_id",id).put("version_id","BASELINE").put("sha256",baselineSha).put("coin_file",coin.name))
    else versions.forEach{v->coins.put(JSONObject().put("coin_id",id).put("version_id",v.optString("version_id","UNRESOLVED")).put("parent_version",v.optString("parent_version","UNRESOLVED")).put("sha256",v.optString("sha256","UNRESOLVED")).put("artifact",v.optString("name","UNRESOLVED")).put("coin_file",coin.name))}
   }catch(_:Exception){}
  }
  File(dir,"scenario.json").writeText(JSONObject().put("mode",run.mode.name).put("seed",run.seed).put("tick_rate",run.tickRate).put("inputs",JSONArray(run.inputs)).put("coin_assembly",coins).toString(2))
  val ev=File(dir,"events.jsonl");ev.bufferedWriter().use{w->run.events.forEach{e->w.write(JSONObject().put("tick",e.tick).put("type",e.type).put("data",e.data).toString());w.newLine()}}
  File(dir,"state-hashes.json").writeText(JSONObject().put("before",run.beforeHash).put("after",run.afterHash).toString(2))
  File(dir,"report.json").writeText(JSONObject().put("schema","build-by-coin.test-report.v0.1").put("result","COMPLETED").put("authoritative_hash",run.afterHash).put("event_count",run.events.size).put("coin_assembly",coins).toString(2));return dir
 }
}