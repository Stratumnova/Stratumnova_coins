package org.stratumnova.buildbycoin.diagnostics
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
data class DiagnosticIssue(val severity:String,val code:String,val source:String,val message:String)
object BuildDiagnostics {
 private fun sha256(file:File):String { val md=MessageDigest.getInstance("SHA-256");file.inputStream().use{input->val b=ByteArray(65536);while(true){val n=input.read(b);if(n<0)break;md.update(b,0,n)}};return md.digest().joinToString(""){"%02x".format(it)} }
 fun scan(workspace:File,commands:List<String>):File {
  val stamp=System.currentTimeMillis();val out=File(workspace,"diagnostics/report-"+stamp).apply{mkdirs()};val working=File(workspace,"working")
  val files=if(working.exists())working.walkTopDown().filter{it.isFile}.toList() else emptyList();val issues=mutableListOf<DiagnosticIssue>()
  files.filter{it.extension.lowercase()=="json"}.forEach{f->try{JSONObject(f.readText())}catch(e:Exception){issues+=DiagnosticIssue("ERROR","JSON_PARSE",f.relativeTo(workspace).invariantSeparatorsPath,e.message?:"Invalid JSON")}}
  files.filter{it.name.endsWith(".coin.json")}.forEach{f->try{val j=JSONObject(f.readText());if(j.optString("coin_id").isBlank())issues+=DiagnosticIssue("WARNING","COIN_ID",f.relativeTo(workspace).invariantSeparatorsPath,"Coin has no coin_id")}catch(_:Exception){}}
  val manifest=JSONObject().put("schema","stratumnova.ai-build-report.v0.1").put("created_ms",stamp).put("status",if(issues.any{it.severity=="ERROR"})"FAILED" else "SCAN_OK").put("workspace_version","0.1.0").put("file_count",files.size).put("coin_count",files.count{it.name.endsWith(".coin.json")}).put("asset_count",files.count{it.extension.lowercase() in setOf("png","jpg","jpeg","webp","wav","mp3","ogg")}).put("commands",JSONArray(commands.takeLast(50))).put("issues",JSONArray().apply{issues.forEach{put(JSONObject().put("severity",it.severity).put("code",it.code).put("source",it.source).put("message",it.message))}})
  File(out,"report.json").writeText(manifest.toString(2))
  File(out,"report.txt").writeText(buildString{
   appendLine("STRATUMNOVA BUILD & ERROR REPORT");appendLine("Status: "+manifest.getString("status"));appendLine("Files: "+files.size+" | Coins: "+manifest.getInt("coin_count")+" | Assets: "+manifest.getInt("asset_count"));appendLine();appendLine("ISSUES")
   if(issues.isEmpty())appendLine("No static workspace errors found.")
   issues.forEachIndexed{index,x->appendLine("["+(index+1)+"] "+x.severity+" "+x.code+"\n    "+x.source+"\n    "+x.message)}
   appendLine();appendLine("LAST COMMANDS");commands.takeLast(50).forEach{appendLine("> "+it)};appendLine();appendLine("FILE INDEX")
   files.take(1000).forEach{appendLine(it.relativeTo(workspace).invariantSeparatorsPath+" | "+it.length()+" B | "+sha256(it))}
  });return out
 }
 fun latest(workspace:File):File?=File(workspace,"diagnostics").listFiles()?.filter{it.isDirectory&&File(it,"report.txt").isFile}?.maxByOrNull{it.lastModified()}
}