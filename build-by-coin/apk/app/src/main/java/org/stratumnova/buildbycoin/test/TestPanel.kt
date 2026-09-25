package org.stratumnova.buildbycoin.test
import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.*
import android.graphics.drawable.GradientDrawable
import android.graphics.Typeface
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
  val cyan=Color.rgb(0,213,255);val panel=Color.rgb(5,18,29)
  fun bg()=GradientDrawable().apply{setColor(panel);cornerRadius=14f;setStroke(2,cyan)}
  fun button(t:String)=Button(a).apply{text=t;textSize=13f;setTextColor(Color.WHITE);background=bg();isAllCaps=false}
  val root=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
  root.addView(TextView(a).apply{text="RUN LAB";textSize=22f;setTextColor(cyan);setTypeface(typeface,Typeface.BOLD);setPadding(8,8,8,8)})
  val top=LinearLayout(a);top.addView(button("←  Back").apply{setOnClickListener{onBack()}},LinearLayout.LayoutParams(0,-2,1f))
  val status=TextView(a).apply{setTextColor(Color.WHITE);textSize=15f;text="PLAY / AUTO / WATCH — same RuntimeCore | active artifacts "+assembly.size+" | adapters "+contributions.size+" | step "+moveStep+" | tick "+tickRate+(if(assemblyResult.errors.isNotEmpty()) " | REJECTED "+assemblyResult.errors.size else "")+(if(conflicts.isNotEmpty()) " | CONFIG CONFLICT "+conflicts.joinToString(",") else "")};top.addView(status,LinearLayout.LayoutParams(0,-2,3f));root.addView(top)
  val game=GameView(a,runtime);game.onCommand={cmd->playInputs+=cmd;status.text="PLAY | inputs "+playInputs.size+" | tick "+runtime.clock.tick};root.addView(game,LinearLayout.LayoutParams(-1,0,1f))
  val sessionControls=LinearLayout(a)
  sessionControls.addView(button("RESET").apply{setOnClickListener{if(!runBusy){runtime.resetSession();game.invalidate();status.text="Session reset | "+runtime.activePreset}}},LinearLayout.LayoutParams(0,-2,1f))
  sessionControls.addView(button("KEEP RUNNING").apply{setOnClickListener{status.text="State retained | tick "+runtime.clock.tick+" | "+runtime.activePreset}},LinearLayout.LayoutParams(0,-2,1f))
  root.addView(sessionControls)
  val presets=LinearLayout(a)
  listOf("MARKET","FISHING","COMBAT","SANDBOX").forEach{preset->
   presets.addView(button(preset).apply{setOnClickListener{
    if(runBusy)status.text="Runtime busy — preset change blocked"
    else{runtime.applyRoomPreset(preset,true);game.invalidate();status.text="ROOM "+preset+" | 25x25 | player "+(runtime.room.tile(runtime.state.playerX,runtime.state.playerY)?.spaceCode?:"?")}
   }},LinearLayout.LayoutParams(0,-2,1f))
  }
  root.addView(presets)
  val environmentTools=LinearLayout(a)
  environmentTools.addView(button("ENVIRONMENT").apply{setOnClickListener{
   val scopes=TestScope.values();val weathers=WeatherKind.values();val phases=DayPhase.values()
   val items=arrayOf("Scope: "+runtime.environment.scope.name,"Weather: "+runtime.environment.weather.name,"Day/Night: "+runtime.environment.dayPhase.name,"Cycle: "+if(runtime.environment.cycleEnabled)"ON" else "OFF")
   android.app.AlertDialog.Builder(a).setTitle("Test environment").setItems(items){_,which->
    when(which){
     0->runtime.setEnvironment(scopes[(runtime.environment.scope.ordinal+1)%scopes.size],runtime.environment.targetId,runtime.environment.weather,runtime.environment.dayPhase,runtime.environment.cycleEnabled,runtime.environment.hudName)
     1->runtime.setEnvironment(runtime.environment.scope,runtime.environment.targetId,weathers[(runtime.environment.weather.ordinal+1)%weathers.size],runtime.environment.dayPhase,runtime.environment.cycleEnabled,runtime.environment.hudName)
     2->runtime.setEnvironment(runtime.environment.scope,runtime.environment.targetId,runtime.environment.weather,phases[(runtime.environment.dayPhase.ordinal+1)%phases.size],runtime.environment.cycleEnabled,runtime.environment.hudName)
     3->runtime.setEnvironment(runtime.environment.scope,runtime.environment.targetId,runtime.environment.weather,runtime.environment.dayPhase,!runtime.environment.cycleEnabled,runtime.environment.hudName)
    };game.invalidate();status.text="ENV | "+runtime.environment.scope+" | "+runtime.environment.weather+" | "+runtime.environment.dayPhase
   }.show()
  }},LinearLayout.LayoutParams(0,-2,1f))
  environmentTools.addView(button("EDIT").apply{setOnClickListener{
   val box=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setPadding(24,8,24,8)}
   val hud=EditText(a).apply{setText(runtime.environment.hudName);hint="HUD name"}
   val target=EditText(a).apply{setText(runtime.environment.targetId);hint="Target / group name"}
   box.addView(TextView(a).apply{text="HUD / DISPLAY";setTextColor(cyan)});box.addView(hud)
   box.addView(TextView(a).apply{text="ENVIRONMENT TARGET";setTextColor(cyan)});box.addView(target)
   android.app.AlertDialog.Builder(a).setTitle("EDIT SETTINGS").setView(box).setPositiveButton("SAVE"){_,_->
    runtime.setEnvironment(runtime.environment.scope,target.text.toString(),runtime.environment.weather,runtime.environment.dayPhase,runtime.environment.cycleEnabled,hud.text.toString())
    game.invalidate();status.text="EDIT saved | HUD "+runtime.environment.hudName+" | target "+runtime.environment.targetId
   }.setNegativeButton("CANCEL",null).show()
  }},LinearLayout.LayoutParams(0,-2,1f))
  root.addView(environmentTools)
  val tileTools=LinearLayout(a)
  tileTools.addView(button("SPACE CODES").apply{setOnClickListener{game.showSpaceCodes=!game.showSpaceCodes;game.invalidate();status.text="Space codes "+if(game.showSpaceCodes)"ON" else "OFF"}},LinearLayout.LayoutParams(0,-2,1f))
  tileTools.addView(button("TILE HERE").apply{setOnClickListener{val t=runtime.room.tile(runtime.state.playerX,runtime.state.playerY);status.text=(t?.spaceCode?:"UNRESOLVED")+" | "+(t?.kind?.name?:"UNRESOLVED")}},LinearLayout.LayoutParams(0,-2,1f))
  tileTools.addView(button("SWAP TILE").apply{setOnClickListener{
   val t=runtime.room.tile(runtime.state.playerX,runtime.state.playerY)?:return@setOnClickListener
   val kinds=TileKind.values()
   android.app.AlertDialog.Builder(a).setTitle("Tile "+t.spaceCode).setItems(kinds.map{it.name}.toTypedArray()){_,which->
    runtime.setTile(t.spaceCode,kinds[which]);game.invalidate();status.text=t.spaceCode+" → "+kinds[which].name
   }.show()
  }},LinearLayout.LayoutParams(0,-2,1f))
  root.addView(tileTools)
  val marketControls=LinearLayout(a)
  fun marketSummary():String{
   val stock=runtime.market.stalls.values.joinToString("  "){it.id.replace("STALL-","S")+":"+it.stock}
   return "MARKET | credits "+runtime.market.marketCredits+" | stock "+stock
  }
  listOf("STALL-1","STALL-2","STALL-3").forEachIndexed{i,stallId->
   marketControls.addView(button("CRAFT S"+(i+1)).apply{setOnClickListener{
    val ok=runtime.craft(stallId);game.invalidate()
    status.text=(if(ok)"CRAFT OK | " else "CRAFT REJECTED | ")+stallId+" | "+marketSummary()
   }},LinearLayout.LayoutParams(0,-2,1f))
  }
  root.addView(marketControls)
  val saleControls=LinearLayout(a)
  listOf("STALL-1","STALL-2","STALL-3").forEachIndexed{i,stallId->
   val customerId="CUSTOMER-"+(i+1)
   saleControls.addView(button("SELL S"+(i+1)+"→C"+(i+1)).apply{setOnClickListener{
    val ok=runtime.sell(stallId,customerId);game.invalidate()
    status.text=(if(ok)"SALE OK | " else "SALE REJECTED | ")+stallId+" → "+customerId+" | "+marketSummary()
   }},LinearLayout.LayoutParams(0,-2,1f))
  }
  root.addView(saleControls)
  val npcControls=LinearLayout(a)
  npcControls.addView(button("NPC STEP").apply{setOnClickListener{
   if(runBusy)status.text="Runtime busy — NPC step blocked"
   else{val action=runtime.marketAutoStep();game.invalidate();status.text="NPC STEP | "+action+" | credits "+runtime.market.marketCredits}
  }},LinearLayout.LayoutParams(0,-2,1f))
  npcControls.addView(button("MARKET ×6").apply{setOnClickListener{
   if(runBusy)status.text="Runtime busy — market cycle blocked"
   else{val actions=mutableListOf<String>();repeat(6){actions+=runtime.marketAutoStep()};game.invalidate();status.text="MARKET ×6 | credits "+runtime.market.marketCredits+" | "+actions.last()}
  }},LinearLayout.LayoutParams(0,-2,1f))
  root.addView(npcControls)
  val controls=LinearLayout(a)
  controls.addView(button("PLAY").apply{setOnClickListener{if(assemblyResult.errors.isNotEmpty()){status.text="PLAY blocked — assembly verification failed: "+assemblyResult.errors.joinToString(",")}else if(conflicts.isNotEmpty()){status.text="PLAY blocked — runtime_config conflict: "+conflicts.joinToString(",")}else if(!runBusy){playInputs.clear();playStart=runtime.stateHash();playStartEvents=runtime.bus.all().size;status.text="PLAY armed — touch game to move"}}},LinearLayout.LayoutParams(0,-2,1f))
  controls.addView(button("SAVE").apply{setOnClickListener{if(assemblyResult.errors.isNotEmpty()||conflicts.isNotEmpty()){status.text="SAVE blocked — invalid assembly";return@setOnClickListener};val r=TestRun(RunMode.PLAY,runtime.seed,runtime.tickRate,playInputs.map{it.name},playStart,runtime.stateHash(),runtime.bus.all().drop(playStartEvents),contributions,assembly);val d=EvidenceWriter.write(File(a.filesDir,"workspace/evidence"),r);status.text="PLAY saved | "+playInputs.size+" inputs | "+d.name}},LinearLayout.LayoutParams(0,-2,1f))
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
  controls.addView(button("AUTO").apply{setOnClickListener{automated(RunMode.AUTO)}},LinearLayout.LayoutParams(0,-2,1f))
  controls.addView(button("WATCH").apply{setOnClickListener{watch()}},LinearLayout.LayoutParams(0,-2,1f));root.addView(controls);a.setContentView(root)
 }
}