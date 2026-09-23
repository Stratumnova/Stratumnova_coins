package org.stratumnova.buildbycoin.test
import android.app.Activity
import android.graphics.Color
import android.widget.*
import org.stratumnova.buildbycoin.runtime.*
import java.io.File
object TestPanel{
 fun show(a:Activity){
  val runtime=RuntimeCore(25L,30);val root=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
  val status=TextView(a).apply{setTextColor(Color.WHITE);textSize=15f;text="Same RuntimeCore for PLAY / AUTO / WATCH"}
  val game=GameView(a,runtime);root.addView(status);root.addView(game,LinearLayout.LayoutParams(-1,0,1f))
  val controls=LinearLayout(a)
  fun add(name:String,mode:RunMode,commands:List<Command>){controls.addView(Button(a).apply{text=name;setOnClickListener{val run=TestHarness(runtime).run(mode,commands);val dir=EvidenceWriter.write(File(a.filesDir,"workspace/evidence"),run);game.invalidate();status.text=mode.name+" complete | events "+run.events.size+" | "+run.afterHash.take(12)+"\n"+dir.name}},LinearLayout.LayoutParams(0,-2,1f))}
  add("PLAY",RunMode.PLAY,emptyList());add("AUTO",RunMode.AUTO,TestHarness.defaultScenario());add("WATCH",RunMode.WATCH,TestHarness.defaultScenario());root.addView(controls);a.setContentView(root)
 }
}