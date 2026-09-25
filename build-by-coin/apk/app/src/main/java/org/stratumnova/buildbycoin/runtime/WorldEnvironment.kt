package org.stratumnova.buildbycoin.runtime

enum class TestScope { WORLD, ROOM, SPRITE }
enum class WeatherKind { CLEAR, CLOUDY, RAIN, STORM, SNOW, FOG }
enum class DayPhase { DAWN, DAY, DUSK, NIGHT }

data class EnvironmentState(
 var scope:TestScope=TestScope.ROOM,
 var targetId:String="TEST-ROOM",
 var weather:WeatherKind=WeatherKind.CLEAR,
 var dayPhase:DayPhase=DayPhase.DAY,
 var cycleEnabled:Boolean=false,
 var cycleTicks:Int=300,
 var hudName:String="ROOM"
){
 fun step(tick:Long){
  if(!cycleEnabled||cycleTicks<1)return
  val phases=DayPhase.values()
  dayPhase=phases[((tick/cycleTicks)%phases.size).toInt()]
 }
 fun reset(){scope=TestScope.ROOM;targetId="TEST-ROOM";weather=WeatherKind.CLEAR;dayPhase=DayPhase.DAY;cycleEnabled=false;cycleTicks=300;hudName="ROOM"}
}
