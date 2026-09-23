package org.stratumnova.buildbycoin.runtime
enum class Command{UP,DOWN,LEFT,RIGHT,WAIT}
class RuntimeCore(val seed:Long=25L,val tickRate:Int=30){
 val clock=GameClock(tickRate);val rng=SeededRng(seed);val bus=EventBus();val state=WorldState()
 private val walls=setOf(0 to 0,1 to 0,2 to 0,3 to 0,4 to 0,0 to 1,4 to 1,0 to 2,4 to 2,0 to 3,4 to 3,0 to 4,1 to 4,2 to 4,3 to 4,4 to 4)
 fun command(c:Command){val ox=state.playerX;val oy=state.playerY;var nx=ox;var ny=oy;when(c){Command.UP->ny--;Command.DOWN->ny++;Command.LEFT->nx--;Command.RIGHT->nx++;Command.WAIT->{}}
 if((nx to ny)!in walls){state.playerX=nx;state.playerY=ny};clock.step();state.ticks=clock.tick;bus.emit(GameEvent(clock.tick,"COMMAND",c.name));if(ox!=state.playerX||oy!=state.playerY)bus.emit(GameEvent(clock.tick,"MOVE",state.playerX.toString()+","+state.playerY))}
 fun stateHash():String{val s=state.playerX.toString()+"|"+state.playerY+"|"+state.score+"|"+state.ticks+"|"+rng.stateValue();val m=java.security.MessageDigest.getInstance("SHA-256");return m.digest(s.toByteArray()).joinToString(""){"%02x".format(it)}}
}