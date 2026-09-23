package org.stratumnova.buildbycoin

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.content.Intent
import android.net.Uri
import android.widget.*
import org.stratumnova.buildbycoin.intake.IntakeScanner

class MainActivity : Activity() {
    private lateinit var terminal: TextView
    private val OPEN_FILE=1001
    private val OPEN_ZIP=1002
    private val OPEN_ASSET=1003

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(16,16,16,16); setBackgroundColor(Color.rgb(2,7,13)) }
        fun label(t:String,s:Float=16f)=TextView(this).apply{text=t;textSize=s;setTextColor(Color.WHITE);setPadding(8,8,8,8)}
        root.addView(label("BUILD BY COIN",24f)); root.addView(label("COIN TESTING & GAME BUILDER",12f))
        val tabs=LinearLayout(this); listOf("Terminal","Files","Assets","Build","Run").forEach { tabs.addView(Button(this).apply{text=it}) }; root.addView(tabs)
        terminal=label("> Welcome to Build by Coin\n> Ready for intake\n> Runtime: v0.1.0\n> Originals remain untouched.",15f)
        root.addView(terminal,LinearLayout.LayoutParams(-1,0,1f))
        root.addView(label("INTAKE",18f))
        val intake=LinearLayout(this)
        fun button(n:String, code:Int, type:String)=Button(this).apply { text=n; setOnClickListener { pick(code,type) } }
        intake.addView(button("FILE",OPEN_FILE,"*/*"),LinearLayout.LayoutParams(0,-2,1f))
        intake.addView(button("ZIP",OPEN_ZIP,"application/zip"),LinearLayout.LayoutParams(0,-2,1f))
        intake.addView(button("ASSET",OPEN_ASSET,"*/*"),LinearLayout.LayoutParams(0,-2,1f))
        root.addView(intake); root.addView(label("Single files for quick intake; ZIP for cartridges and large sets.",14f))
        setContentView(root)
    }

    private fun pick(code:Int,type:String) {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); this.type=type },code)
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(resultCode!=RESULT_OK) return
        val uri=data?.data ?: return
        try {
            val scanner=IntakeScanner(this)
            if(requestCode==OPEN_ZIP) {
                val items=scanner.scanZip(uri)
                val groups=items.groupingBy{it.kind}.eachCount()
                terminal.text="> ZIP scanned\n> Entries: ${items.size}\n"+groups.entries.sortedByDescending{it.value}.joinToString("\n"){"> ${it.key}: ${it.value}"}+"\n> Source ZIP not modified."
            } else {
                val f=scanner.scanSingle(uri)
                terminal.text="> File scanned\n> ${f.name}\n> Type: ${f.kind}\n> Bytes: ${f.bytes}\n> SHA-256: ${f.sha256}\n> Source file not modified."
            }
        } catch(e:Exception) { terminal.text="> INTAKE ERROR\n> ${e.javaClass.simpleName}: ${e.message}" }
    }
}
