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
  val assemblyResult=ActiveAssembly.resolveVerified(File(a.filesDir,"workspace/working"));val assembly=assemblyResult.artifacts;val contributions=RuntimeAdapter.adaptAll(assembly)
  val runtimeConfigs=contributions.filter{it.kind=="runtime_config"}
  val conflicts=runtimeConfigs.flatMap{it.values.keys}.distinct().filter{k->runtimeConfigs.mapNotNull{it.values[k]}.distinct().size>1}
  val config=runtimeConfigs.fold(mutableMapOf<String,String>()){acc,x->acc.putAll(x.values);acc}
  val seed=config["seed"]?.toLongOrNull()?:25L
  val tickRate=(config["tick_rate"]?.toIntOrNull()?:30).coerceIn(1,240)
  val moveStep=(config["move_step"]?.toIntOrNull()?:1).coerceIn(1,3)
  val runtime=RuntimeCore(seed,tickRate,moveStep);val playInputs=mutableListOf<Command>();var playStart=runtime.stateHash();var playStartEvents=0;var runBusy=false
  val root=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
  val top=LinearLayout(a);top.addView(Button(a).apply{text="← BACK";setOnClickListener{onBack()}},LinearLayout.LayoutParams(0,-2,1f))
  val status=TextView(a).apply{setTextColor(Color.WHITE);textSize=15f;text="PLAY / AUTO / WATCH — same RuntimeCore | active artifacts "+assembly.size+" | adapters "+contributions.size+" | step "+moveStep+" | tick "+tickRate+(if(assemblyResult.errors.isNotEmpty()) " | REJECTED "+assemblyResult.errors.size else "")+(if(conflicts.isNotEmpty()) " | CONFIG CONFLICT "+conflicts.joinToString(",") else "")};top.addView(status,LinearLayout.LayoutParams(0,-2,3f));root.addView(top)
  val game=GameView(a,runtime);game.onCommand={cmd->playInputs+=cmd;status.text="PLAY | inputs "+playInputs.size+" | tick "+runtime.clock.tick};root.addView(game,LinearLayout.LayoutParams(-1,0,1f))
  val controls=LinearLayout(a)
  controls.addView(Button(a).apply{text="PLAY";setOnClickListener{if(assemblyResult.errors.isNotEmpty()){status.text="PLAY blocked — assembly verification failed: "+assemblyResult.errors.joinToString(",")}else if(conflicts.isNotEmpty()){status.text="PLAY blocked — runtime_config conflict: "+conflicts.joinToString(",")}else if(!runBusy){playInputs.clear();playStart=runtime.stateHash();playStartEvents=runtime.bus.all().size;status.text="PLAY armed — touch game to move"}}},LinearLayout.LayoutParams(0,-2,1f))
  controls.addView(Button(a).apply{text="SAVE";setOnClickListener{if(assemblyResult.errors.isNotEmpty()||conflicts.isNotEmpty()){status.text="SAVE blocked — invalid assembly";return@setOnClickListener};val r=TestRun(RunMode.PLAY,runtime.seed,runtime.tickRate,playInputs.map{it.name},playStart,runtime.stateHash(),runtime.bus.all().drop(playStartEvents),contributions,assembly);val d=EvidenceWriter.write(File(a.filesDir,"workspace/evidence"),r);status.text="PLAY saved | "+playInputs.size+" inputs | "+d.name}},LinearLayout.LayoutParams(0,-2,1f))
  fun automated(mode:RunMode){if(runBusy){status.text="Runtime busy — wait for current run";return};if(assemblyResult.errors.isNotEmpty()){status.text="AUTO blocked — assembly verification failed: "+assemblyResult.errors.joinToString(",");return};if(conflicts.isNotEmpty()){status.text="AUTO blocked — runtime_config conflict: "+conflicts.joinToString(",");return};runBusy=true;val base=TestHarness(runtime).run(mode,TestHarness.defaultScenario());val r=base.copy(runtimeContributions=contributions,activeAssembly=assembly);val d=EvidenceWriter.write(File(a.filesDir,"workspace/evidence"),r);game.invalidate();runBusy=false;status.text=mode.name+" complete | events "+r.events.size+" | "+r.afterHash.take(12)+"\n"+d.name}
  fun watch(){
   if(runBusy){status.text="Runtime busy — wait for current run";return}
   if(assemblyResult.errors.isNotEmpty()){status.text="WATCH blocked — assembly verification failed: "+assemblyResult.errors.joinToString(",");return}
   if(conflicts.isNotEmpty()){status.text="WATCH blocked — runtime_config conflict: "+conflicts.joinToString(",");return}
   runBusy=true
   val commands=TestHarness.defaultScenario();val before=runtime.stateHash();val startEvents=runtime.bus.all().size;var i=0
   val handler=Handler(Looper.getMainLooper())
   fun step(){
    if(i<commands.size){
     val cmd=commands[i++];runtime.command(cmd);game.invalidate();status.text="WATCH | step "+i+"/"+commands.size+" | "+cmd.name+" | tick "+runtime.clock.tick
     handler.postDelayed({step()},450)
    }else{
     val events=runtime.bus.all().drop(startEvents)
     val r=TestRun(RunMode.WATCH,runtime.seed,runtime.tickRate,commands.map{it.name},before,runtime.stateHash(),events,contributions,assembly)
     val d=EvidenceWriter.write(File(a.filesDir,"workspace/evidence"),r)
     runBusy=false;status.text="WATCH complete | events "+events.size+" | "+r.afterHash.take(12)+"\n"+d.name
    }
   }
   status.text="WATCH armed | deterministic scenario";step()
  }
  controls.addView(Button(a).apply{text="AUTO";setOnClickListener{automated(RunMode.AUTO)}},LinearLayout.LayoutParams(0,-2,1f))
  controls.addView(Button(a).apply{text="WATCH";setOnClickListener{watch()}},LinearLayout.LayoutParams(0,-2,1f));root.addView(controls);a.setContentView(root)
 }
}