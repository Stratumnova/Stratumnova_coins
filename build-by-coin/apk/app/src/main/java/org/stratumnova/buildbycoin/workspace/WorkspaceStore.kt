package org.stratumnova.buildbycoin.workspace
import android.content.Context
import android.net.Uri
import org.stratumnova.buildbycoin.intake.IntakeEntry
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipInputStream
data class StoredItem(val file:File,val entry:IntakeEntry)
data class CartridgeResult(val root:File,val files:List<File>)
class WorkspaceStore(private val context:Context){
 private val ws=Workspace(File(context.filesDir,"workspace")).also{it.ensure()}
 fun preserveSingle(uri:Uri,e:IntakeEntry):StoredItem{val target=unique(File(ws.originals,safe(e.name)));context.contentResolver.openInputStream(uri)!!.use{i->target.outputStream().use{o->i.copyTo(o)}};verify(target,e);return StoredItem(target,e)}
 fun importZip(uri:Uri):CartridgeResult{
  val root=uniqueDir(File(ws.working,"cartridge"));root.mkdirs();val files=mutableListOf<File>()
  context.contentResolver.openInputStream(uri)!!.use{raw->ZipInputStream(raw).use{zip->while(true){val e=zip.nextEntry?:break;if(!e.isDirectory){val out=File(root,e.name).canonicalFile;require(out.path.startsWith(root.canonicalPath+File.separator)){"unsafe ZIP path"};out.parentFile?.mkdirs();out.outputStream().use{o->zip.copyTo(o)};files+=out};zip.closeEntry()}}}
  return CartridgeResult(root,files)
 }
 fun listOriginals()=ws.originals.walkTopDown().filter{it.isFile}.sortedBy{it.name.lowercase()}.toList()
 private fun verify(file:File,e:IntakeEntry){require(file.length()==e.bytes);val m=MessageDigest.getInstance("SHA-256");file.inputStream().use{i->val b=ByteArray(65536);while(true){val n=i.read(b);if(n<0)break;m.update(b,0,n)}};require(m.digest().joinToString(""){"%02x".format(it)}==e.sha256)}
 private fun safe(n:String)=n.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"),"_").ifBlank{"unnamed"}
 private fun unique(f:File):File{if(!f.exists())return f;var i=1;while(true){val suffix=if(f.extension.isBlank())"" else "."+f.extension;val x=File(f.parentFile,f.nameWithoutExtension+"_"+i+suffix);if(!x.exists())return x;i++}}
 private fun uniqueDir(f:File):File{if(!f.exists())return f;var i=1;while(true){val x=File(f.parentFile,f.name+"_"+i);if(!x.exists())return x;i++}}
}