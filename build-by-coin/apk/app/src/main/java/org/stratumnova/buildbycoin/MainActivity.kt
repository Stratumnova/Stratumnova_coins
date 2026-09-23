package org.stratumnova.buildbycoin

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16,16,16,16)
            setBackgroundColor(Color.rgb(2,7,13))
        }
        fun label(text:String, size:Float=16f) = TextView(this).apply {
            this.text=text; textSize=size; setTextColor(Color.WHITE); setPadding(8,8,8,8)
        }
        root.addView(label("BUILD BY COIN",24f))
        root.addView(label("COIN TESTING & GAME BUILDER",12f))
        val tabs=LinearLayout(this)
        listOf("Terminal","Files","Assets","Build","Run").forEach { n ->
            tabs.addView(Button(this).apply { text=n })
        }
        root.addView(tabs)
        root.addView(label("> Welcome to Build by Coin\n> Ready for intake\n> Runtime: v0.1.0\n> Waiting...",15f),
            LinearLayout.LayoutParams(-1,0,1f))
        root.addView(label("INTAKE",18f))
        val intake=LinearLayout(this)
        listOf("FILE","ZIP","ASSET").forEach { n -> intake.addView(Button(this).apply { text=n },
            LinearLayout.LayoutParams(0,-2,1f)) }
        root.addView(intake)
        root.addView(label("No project loaded. Import a single file or ZIP cartridge.",14f))
        setContentView(root)
    }
}
