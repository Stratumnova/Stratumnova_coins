package org.stratumnova.buildbycoin.test
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
object EvidenceWriter{
 fun write(root:File,run:TestRun):File{root.mkdirs();val dir=File(root,"run_"+System.currentTimeMillis());dir.mkdirs()
  val coins=JSONArray()
  run.activeAssembly.forEach{x->coins.put(JSONObject().put("coin_id",x.coinId).put("version_id",x.versionId).put("sha256",x.sha256).put("artifact",x.artifact.name))}
  val applied=JSONArray();run.runtimeContributions.forEach{c->applied.put(JSONObject().put("coin_id",c.coinId).put("version_id",c.versionId).put("artifact",c.artifactName).put("kind",c.kind).put("values",JSONObject(c.values)))}
  File(dir,"scenario.json").writeText(JSONObject().put("mode",run.mode.name).put("seed",run.seed).put("tick_rate",run.tickRate).put("inputs",JSONArray(run.inputs)).put("coin_assembly",coins).put("runtime_contributions",applied).toString(2))
  val ev=File(dir,"events.jsonl");ev.bufferedWriter().use{w->run.events.forEach{e->w.write(JSONObject().put("tick",e.tick).put("type",e.type).put("data",e.data).toString());w.newLine()}}
  File(dir,"state-hashes.json").writeText(JSONObject().put("before",run.beforeHash).put("after",run.afterHash).toString(2))
  File(dir,"report.json").writeText(JSONObject().put("schema","build-by-coin.test-report.v0.1").put("result","COMPLETED").put("authoritative_hash",run.afterHash).put("event_count",run.events.size).put("coin_assembly",coins).put("runtime_contributions",applied).toString(2));return dir
 }
}