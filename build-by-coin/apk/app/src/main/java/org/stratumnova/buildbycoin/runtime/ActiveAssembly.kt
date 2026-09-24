package org.stratumnova.buildbycoin.runtime

import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

data class ActiveArtifact(val coinId:String,val versionId:String,val artifact:File,val sha256:String)
data class AssemblyResult(val artifacts:List<ActiveArtifact>,val errors:List<String>)

object ActiveAssembly {
 private fun hash(file:File)=MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString(""){"%02x".format(it)}
 fun resolveVerified(working:File):AssemblyResult{
  val errors=mutableListOf<String>()
  val raw=resolveRaw(working)
  val good=raw.filter{a->if(a.sha256=="UNRESOLVED"){errors+="UNRESOLVED_SHA:"+a.coinId+"@"+a.versionId;false}else{val ok=hash(a.artifact).equals(a.sha256,true);if(!ok)errors+="SHA_MISMATCH:"+a.coinId+"@"+a.versionId;ok}}
  return AssemblyResult(good,errors)
 }
 fun resolve(working:File)=resolveVerified(working).artifacts
 private fun resolveRaw(working:File):List<ActiveArtifact>{
  if(!working.exists())return emptyList()
  return working.walkTopDown().filter{it.isFile&&it.name.endsWith(".coin.json")}.mapNotNull{coin->
   try{
    val j=JSONObject(coin.readText());val id=j.optString("coin_id","UNRESOLVED");val cur=j.optJSONObject("current")
    val activeFile=File(coin.parentFile,"active-version.json")
    val active=if(activeFile.isFile)JSONObject(activeFile.readText()).optString("version_id","BASELINE") else "BASELINE"
    if(active=="BASELINE"){
     val name=cur?.optString("name","")?:"";val artifact=File(coin.parentFile,name)
     artifact.takeIf{it.isFile}?.let{ActiveArtifact(id,"BASELINE",it,cur?.optString("sha256","UNRESOLVED")?:"UNRESOLVED")}
    }else{
     val side=File(coin.parentFile,"sideways")
     val v=side.listFiles()?.filter{it.isFile&&it.name.endsWith(".sideways.json")}?.mapNotNull{r->try{JSONObject(r.readText()).takeIf{it.optString("sideways_type")=="VERSION"&&it.optString("version_id")==active}}catch(_:Exception){null}}?.lastOrNull()
     val name=v?.optString("name","")?:"";val artifact=File(coin.parentFile,"versions/"+name)
     if(v!=null&&artifact.isFile)ActiveArtifact(id,active,artifact,v.optString("sha256","UNRESOLVED")) else null
    }
   }catch(_:Exception){null}
  }.toList()
 }
}
