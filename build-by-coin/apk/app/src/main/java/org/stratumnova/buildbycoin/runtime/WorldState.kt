package org.stratumnova.buildbycoin.runtime
data class WorldState(var playerX:Int=2,var playerY:Int=2,var score:Int=0,var ticks:Long=0)
data class GameEvent(val tick:Long,val type:String,val data:String)
class EventBus{private val events=mutableListOf<GameEvent>();fun emit(e:GameEvent){events+=e};fun all():List<GameEvent> = events.toList();fun clear(){events.clear()}}