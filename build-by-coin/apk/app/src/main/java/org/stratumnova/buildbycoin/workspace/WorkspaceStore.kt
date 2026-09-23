package org.stratumnova.buildbycoin.workspace
import android.content.Context
import android.net.Uri
import org.stratumnova.buildbycoin.intake.IntakeEntry
import java.io.File
import java.security.MessageDigest
data class StoredItem(val file:File,val entry:IntakeEntry)
class WorkspaceStore(private val context:Context){
 private val ws=Workspace(File(context.filesDir,"workspace")).also{it.ensure()}
 fun preserveSingle(uri:Uri,e:IntakeEntry):StoredItem{val target=File(ws.originals,safe(e.name));context.contentResolver.openInputStream(uri)!!.use{i->target.outputStream().use{o->i.copyTo(o)}};verify(target,e);return StoredItem(target,e)}
 fun listOriginals()=ws.originals.walkTopDown().filter{it.isFile}.sortedBy{it.name.lowercase()}.toList()
 private fun verify(file:File,e:IntakeEntry){require(file.length()==e.bytes);val m=MessageDigest.getInstance("SHA-256");file.inputStream().use{i->val b=ByteArray(65536);while(true){val n=i.read(b);if(n<0)break;m.update(b,0,n)}};require(m.digest().joinToString(""){"%02x".format(it)}==e.sha256)}
 private fun safe(n:String)=n.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"),"_").ifBlank{"unnamed"}
}