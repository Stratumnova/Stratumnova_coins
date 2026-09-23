package org.stratumnova.buildbycoin.assets
import java.io.File
data class AssetItem(val file:File,val kind:String)
object AssetBrowser {
 private val image=setOf("png","webp","jpg","jpeg","gif")
 private val audio=setOf("wav","ogg","mp3","m4a","aac","flac")
 fun index(files:List<File>):List<AssetItem> = files.mapNotNull{f->
  val e=f.extension.lowercase()
  when { e in image -> AssetItem(f,"IMAGE"); e in audio -> AssetItem(f,"AUDIO"); else -> null }
 }.sortedBy{it.file.name.lowercase()}
}