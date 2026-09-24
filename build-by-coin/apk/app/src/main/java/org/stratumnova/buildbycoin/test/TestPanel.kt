package org.stratumnova.buildbycoin.test
import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.*
import org.stratumnova.buildbycoin.runtime.*
import java.io.File
object TestPanel{
 fun show(a:Activity,onBack:()->Unit){
  val runtime=RuntimeCore(25L,30);val playInputs=mutableListOf<Command>();var playStart=runtime.stateHash()
  val root=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
  val top=LinearLayout(a);top.addView(Button(a).apply{text="← BACK";setOnClickListener{onBack()}},LinearLayout.LayoutParams(0,-2,1f))
  val status=TextView(a).apply{setTextColor(Color.WHITE);textSize=15f;text="PLAY / AUTO / WATCH — same RuntimeCore"};top.addView(status,LinearLayout.LayoutParams(0,-2,3f));root.addView(top)
  val game=GameView(a,runtime);game.onCommand={cmd->playInputs+=cmd;status.text="PLAY | inputs "+playInputs.size+" | tick "+runtime.clock.tick};root.addView(game,LinearLayout.LayoutParams(-1,0,1f))
  val controls=LinearLayout(a)
  controls.addView(Button(a).apply{text="PLAY";setOnClickListener{playInputs.clear();playStart=runtime.stateHash();status.text="PLAY armed — touch game to move"}},LinearLayout.LayoutParams(0,-2,1f))
  controls.addView(Button(a).apply{text="SAVE";setOnClickListener{val r=TestRun(RunMode.PLAY,runtime.seed,runtime.tickRate,playInputs.map{it.name},playStart,runtime.stateHash(),runtime.bus.all());val d=EvidenceWriter.write(File(a.filesDir,"workspace/evidence"),r);status.text="PLAY saved | "+playInputs.size+" inputs | "+d.name}},LinearLayout.LayoutParams(0,-2,1f))
  fun automated(mode:RunMode){val r=TestHarness(runtime).run(mode,TestHarness.defaultScenario());val d=EvidenceWriter.write(File(a.filesDir,"workspace/evidence"),r);game.invalidate();status.text=mode.name+" complete | events "+r.events.size+" | "+r.afterHash.take(12)+"\n"+d.name}
  fun watch(){
   val commands=TestHarness.defaultScenario();val before=runtime.stateHash();val startEvents=runtime.bus.all().size;var i=0
   val handler=Handler(Looper.getMainLooper())
   fun step(){
    if(i<commands.size){
     val cmd=commands[i++];runtime.command(cmd);game.invalidate();status.text="WATCH | step "+i+"/"+commands.size+" | "+cmd.name+" | tick "+runtime.clock.tick
     handler.postDelayed({step()},450)
    }else{
     val events=runtime.bus.all().drop(startEvents)
     val r=TestRun(RunMode.WATCH,runtime.seed,runtime.tickRate,commands.map{it.name},before,runtime.stateHash(),events)
     val d=EvidenceWriter.write(File(a.filesDir,"workspace/evidence"),r)
     status.text="WATCH complete | events "+events.size+" | "+r.afterHash.take(12)+"\n"+d.name
    }
   }
   status.text="WATCH armed | deterministic scenario";step()
  }
  controls.addView(Button(a).apply{text="AUTO";setOnClickListener{automated(RunMode.AUTO)}},LinearLayout.LayoutParams(0,-2,1f))
  controls.addView(Button(a).apply{text="WATCH";setOnClickListener{watch()}},LinearLayout.LayoutParams(0,-2,1f));root.addView(controls);a.setContentView(root)
 }
}