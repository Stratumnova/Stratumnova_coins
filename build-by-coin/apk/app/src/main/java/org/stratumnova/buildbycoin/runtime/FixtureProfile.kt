package org.stratumnova.buildbycoin.runtime

enum class CameraMode { STATIC, FOLLOW, FORCE_LEFT, FORCE_RIGHT }

data class FixtureProfile(
 val preset:String,
 val camera:CameraMode=CameraMode.STATIC,
 val objective:String="Inspect runtime",
 val controls:String="4-way",
 val variants:List<String> = emptyList()
)

object FixtureProfiles{
 fun forPreset(name:String):FixtureProfile=when(name.uppercase()){
  "RACING"->FixtureProfile("RACING",CameraMode.FOLLOW,"Complete oval laps and checkpoints","steer",listOf("RACING-8"))
  "RACING-8"->FixtureProfile("RACING-8",CameraMode.FOLLOW,"Complete figure-eight laps and crossover","steer",listOf("RACING"))
  "PLATFORMER"->FixtureProfile("PLATFORMER",CameraMode.STATIC,"Test jump distance, landing and hazards","left/right+jump",listOf("PLATFORMER-L","PLATFORMER-R"))
  "PLATFORMER-L"->FixtureProfile("PLATFORMER-L",CameraMode.FORCE_LEFT,"Test forced-left scrolling and jump timing","left/right+jump")
  "PLATFORMER-R"->FixtureProfile("PLATFORMER-R",CameraMode.FORCE_RIGHT,"Test forced-right scrolling and jump timing","left/right+jump")
  "FISHING"->FixtureProfile("FISHING",CameraMode.STATIC,"Cast from shore into fish spawn zones","move+cast")
  "FARMING"->FixtureProfile("FARMING",CameraMode.STATIC,"Plant, tend and harvest crop plots","move+interact")
  "COMBAT"->FixtureProfile("COMBAT",CameraMode.FOLLOW,"Move, target and use cover","move+action")
  "SHOOTING"->FixtureProfile("SHOOTING",CameraMode.STATIC,"Acquire near, mid and far targets","aim+action")
  "MARKET"->FixtureProfile("MARKET",CameraMode.STATIC,"Craft stock and complete customer sales","move+interact")
  else->FixtureProfile(name.uppercase())
 }
}
