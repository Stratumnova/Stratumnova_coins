package org.stratumnova.buildbycoin.runtime

import org.json.JSONObject

data class RuntimeContribution(
 val coinId:String,
 val versionId:String,
 val artifactName:String,
 val kind:String,
 val values:Map<String,String>
)

object RuntimeAdapter {
 fun adapt(a:ActiveArtifact):RuntimeContribution? {
  val ext=a.artifact.extension.lowercase()
  if(ext!="json")return null
  return try{
   val j=JSONObject(a.artifact.readText())
   val runtime=j.optJSONObject("build_by_coin_runtime")?:return null
   val kind=runtime.optString("kind","UNRESOLVED")
   if(kind=="UNRESOLVED")return null
   val values=mutableMapOf<String,String>()
   val keys=runtime.keys();while(keys.hasNext()){val k=keys.next();if(k!="kind")values[k]=runtime.opt(k)?.toString()?:"UNRESOLVED"}
   RuntimeContribution(a.coinId,a.versionId,a.artifact.name,kind,values)
  }catch(_:Exception){null}
 }
 fun adaptAll(assembly:List<ActiveArtifact>)=assembly.mapNotNull{adapt(it)}
}
