package org.stratumnova.buildbycoin.export
import java.io.File
class ExportCatalog(private val workspace:File){
 fun files():List<File> = workspace.walkTopDown().filter{it.isFile}.sortedBy{it.relativeTo(workspace).path.lowercase()}.toList()
 fun latestEvidence():File? = File(workspace,"evidence").listFiles()?.filter{it.isDirectory}?.maxByOrNull{it.lastModified()}
}