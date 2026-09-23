package org.stratumnova.buildbycoin.runtime
class GameClock(val tickRate:Int=30){var tick:Long=0;private set;fun step(){tick++};fun reset(){tick=0}}