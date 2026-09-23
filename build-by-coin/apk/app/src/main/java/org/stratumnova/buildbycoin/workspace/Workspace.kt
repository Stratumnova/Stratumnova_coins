package org.stratumnova.buildbycoin.workspace

import java.io.File

class Workspace(private val root: File) {
    val originals=File(root,"originals")
    val working=File(root,"working")
    val evidence=File(root,"evidence")
    val exports=File(root,"exports")
    fun ensure() { listOf(root,originals,working,evidence,exports).forEach { if(!it.exists()) it.mkdirs() } }
}
