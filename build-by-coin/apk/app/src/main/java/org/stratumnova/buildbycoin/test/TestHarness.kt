package org.stratumnova.buildbycoin.test
import org.stratumnova.buildbycoin.runtime.*
enum class RunMode{PLAY,AUTO,WATCH}
data class TestRun(val mode:RunMode,val seed:Long,val tickRate:Int,val inputs:List<String>,val beforeHash:String,val afterHash:String,val events:List<GameEvent>,val runtimeContributions:List<RuntimeContribution> = emptyList(),val activeAssembly:List<ActiveArtifact> = emptyList())
class TestHarness(private val runtime:RuntimeCore){
 fun run(mode:RunMode,commands:List<Command>):TestRun{val before=runtime.stateHash();val startEvents=runtime.bus.all().size;val inputs=mutableListOf<String>();commands.forEach{inputs+=it.name;runtime.command(it)};return TestRun(mode,runtime.seed,runtime.tickRate,inputs,before,runtime.stateHash(),runtime.bus.all().drop(startEvents))}
 companion object{fun defaultScenario()=listOf(Command.UP,Command.RIGHT,Command.DOWN,Command.LEFT,Command.WAIT)}
}