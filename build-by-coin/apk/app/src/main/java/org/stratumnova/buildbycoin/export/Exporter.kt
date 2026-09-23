package org.stratumnova.buildbycoin.export
import android.content.Context
import android.net.Uri
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
object Exporter {
 fun projectZip(context:Context,workspace:File,target:Uri){
  context.contentResolver.openOutputStream(target,"w")!!.use{raw->ZipOutputStream(raw).use{zip->
   workspace.walkTopDown().filter{it.isFile}.forEach{f->
    val rel=f.relativeTo(workspace).invariantSeparatorsPath
    zip.putNextEntry(ZipEntry(rel));f.inputStream().use{it.copyTo(zip)};zip.closeEntry()
   }
  }}
 }
 fun singleFile(context:Context,file:File,target:Uri){
  context.contentResolver.openOutputStream(target,"w")!!.use{out->file.inputStream().use{it.copyTo(out)}}
 }
}