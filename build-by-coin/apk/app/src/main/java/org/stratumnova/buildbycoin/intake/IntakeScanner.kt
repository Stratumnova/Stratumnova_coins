package org.stratumnova.buildbycoin.intake

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.security.MessageDigest
import java.util.zip.ZipInputStream

data class IntakeEntry(
    val name: String,
    val bytes: Long,
    val sha256: String,
    val kind: String,
    val container: String? = null
)

class IntakeScanner(private val context: Context) {
    fun scanSingle(uri: Uri): IntakeEntry {
        val name = displayName(uri)
        val digest = MessageDigest.getInstance("SHA-256")
        var count = 0L
        context.contentResolver.openInputStream(uri)!!.use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                digest.update(buffer, 0, n); count += n
            }
        }
        return IntakeEntry(name, count, digest.digest().hex(), kind(name))
    }

    fun scanZip(uri: Uri): List<IntakeEntry> {
        val out = mutableListOf<IntakeEntry>()
        context.contentResolver.openInputStream(uri)!!.use { raw ->
            ZipInputStream(raw).use { zip ->
                while (true) {
                    val e = zip.nextEntry ?: break
                    if (!e.isDirectory) {
                        val digest = MessageDigest.getInstance("SHA-256")
                        var count = 0L
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val n = zip.read(buffer)
                            if (n < 0) break
                            digest.update(buffer,0,n); count += n
                        }
                        out += IntakeEntry(e.name, count, digest.digest().hex(), kind(e.name), "zip")
                    }
                    zip.closeEntry()
                }
            }
        }
        return out
    }

    private fun displayName(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) return it.getString(0)
        }
        return uri.lastPathSegment ?: "unnamed"
    }
    private fun kind(name:String):String = when(name.substringAfterLast('.', "").lowercase()) {
        "zip" -> "ZIP"; "json" -> if(name.endsWith(".coin.json")) "COIN" else "JSON"
        "png","webp","jpg","jpeg" -> "IMAGE"; "ogg","wav","mp3","m4a" -> "AUDIO"
        "js","mjs","kt","java","html","css","xml","gradle" -> "CODE"
        "txt","md","pdf" -> "DOCUMENT"; else -> "OTHER"
    }
    private fun ByteArray.hex()=joinToString(""){"%02x".format(it)}
}
