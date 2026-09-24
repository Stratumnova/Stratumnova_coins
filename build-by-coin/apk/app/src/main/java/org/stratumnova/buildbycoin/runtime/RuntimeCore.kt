package org.stratumnova.buildbycoin.runtime
enum class Command{UP,DOWN,LEFT,RIGHT,WAIT}
class RuntimeCore(val seed:Long=25L,val tickRate:Int=30,val moveStep:Int=1){
 val clock=GameClock(tickRate);val rng=SeededRng(seed);val bus=EventBus();val state=WorldState()
 val room=TestRoom();val market=MarketTestState()
 private fun blocked(x:Int,y:Int)=room.tile(x,y)?.walkable!=true
 fun command(c:Command){val ox=state.playerX;val oy=state.playerY;var nx=ox;var ny=oy;when(c){Command.UP->ny-=moveStep;Command.DOWN->ny+=moveStep;Command.LEFT->nx-=moveStep;Command.RIGHT->nx+=moveStep;Command.WAIT->{}}
 if(!blocked(nx,ny)){state.playerX=nx;state.playerY=ny}
 else if(moveStep>1){
  nx=ox;ny=oy;when(c){Command.UP->ny--;Command.DOWN->ny++;Command.LEFT->nx--;Command.RIGHT->nx++;Command.WAIT->{}}
  if(!blocked(nx,ny)){state.playerX=nx;state.playerY=ny}
 };clock.step();state.ticks=clock.tick;bus.emit(GameEvent(clock.tick,"COMMAND",c.name));if(ox!=state.playerX||oy!=state.playerY)bus.emit(GameEvent(clock.tick,"MOVE",room.tile(state.playerX,state.playerY)?.spaceCode?:"UNRESOLVED"))}
 fun setTile(spaceCode:String,kind:TileKind,assetRef:String?=null):Boolean{val ok=room.setTile(spaceCode,kind,assetRef);if(ok)bus.emit(GameEvent(clock.tick,"TILE_CHANGED",spaceCode+"|"+kind.name));return ok}
 fun addPond(x0:Int,y0:Int,x1:Int,y1:Int,assetRef:String?=null){room.addPond(x0,y0,x1,y1,assetRef);bus.emit(GameEvent(clock.tick,"ROOM_REGION_CHANGED","POND|"+x0+","+y0+"|"+x1+","+y1))}
 fun craft(stallId:String):Boolean{val ok=market.craft(stallId);bus.emit(GameEvent(clock.tick,if(ok)"CRAFT_COMPLETED" else "CRAFT_REJECTED",stallId));return ok}
 fun sell(stallId:String,customerId:String):Boolean{val ok=market.sell(stallId,customerId);bus.emit(GameEvent(clock.tick,if(ok)"SALE_COMPLETED" else "SALE_REJECTED",stallId+"|"+customerId));return ok}
 fun stateHash():String{val tileState=room.tiles.flatMap{it.asList()}.joinToString(";"){it.spaceCode+":"+it.kind+":"+(it.occupiedBy?:"")+":"+(it.assetRef?:"")}
  val entityState=room.entities.values.joinToString(";"){it.id+":"+it.x+","+it.y+":"+(it.assetRef?:"")}
  val economy=market.marketCredits.toString()+"|"+market.customerCredits+"|"+market.bins+"|"+market.stalls.mapValues{it.value.stock}
  val s=state.playerX.toString()+"|"+state.playerY+"|"+state.score+"|"+state.ticks+"|"+rng.stateValue()+"|"+moveStep+"|"+tileState+"|"+entityState+"|"+economy
  val m=java.security.MessageDigest.getInstance("SHA-256");return m.digest(s.toByteArray()).joinToString(""){"%02x".format(it)}}
}