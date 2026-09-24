package org.stratumnova.buildbycoin.runtime
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
class GameView(context:Context,val runtime:RuntimeCore=RuntimeCore()):View(context){
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
 var onCommand:((Command)->Unit)?=null
 var showSpaceCodes=false
 override fun onDraw(c:Canvas){super.onDraw(c);c.drawColor(Color.rgb(7,13,18))
  val room=runtime.room
  val size=Math.min(width.toFloat()/room.width,height.toFloat()/room.height)
  val ox=(width-size*room.width)/2f;val oy=(height-size*room.height)/2f
  for(y in 0 until room.height)for(x in 0 until room.width){
   val t=room.tiles[y][x]
   paint.style=Paint.Style.FILL
   paint.color=when(t.kind){TileKind.BLOCKED->Color.rgb(40,45,49);TileKind.PATH->Color.rgb(94,78,52);TileKind.WATER->Color.rgb(25,86,125);TileKind.GRASS->Color.rgb(45,92,50);TileKind.DIRT->Color.rgb(100,70,45);TileKind.SAND->Color.rgb(145,124,73);else->Color.rgb(61,65,67)}
   c.drawRect(ox+x*size,oy+y*size,ox+(x+1)*size-1f,oy+(y+1)*size-1f,paint)
   if(showSpaceCodes && size>=22f){paint.color=Color.LTGRAY;paint.textSize=Math.max(7f,size*0.18f);c.drawText(t.spaceCode,ox+x*size+1f,oy+(y+0.55f)*size,paint)}
  }
  fun marker(id:String,label:String,shape:Int){
   val e=room.entities[id]?:return;val cx=ox+(e.x+.5f)*size;val cy=oy+(e.y+.5f)*size
   paint.color=Color.rgb(0,213,255);paint.style=Paint.Style.STROKE;paint.strokeWidth=Math.max(1f,size*.08f)
   if(shape==0)c.drawRect(cx-size*.35f,cy-size*.35f,cx+size*.35f,cy+size*.35f,paint) else c.drawCircle(cx,cy,size*.32f,paint)
   paint.style=Paint.Style.FILL;paint.color=Color.WHITE;paint.textSize=Math.max(8f,size*.28f);c.drawText(label,cx-size*.25f,cy+size*.1f,paint)
  }
  marker("STALL-1","S1",0);marker("STALL-2","S2",0);marker("STALL-3","S3",0)
  marker("VENDOR-1","V1",0);marker("VENDOR-2","V2",0);marker("VENDOR-3","V3",0)
  marker("CUSTOMER-1","C1",1);marker("CUSTOMER-2","C2",1);marker("CUSTOMER-3","C3",1)
  paint.color=Color.rgb(0,213,255);paint.style=Paint.Style.FILL;c.drawCircle(ox+(runtime.state.playerX+.5f)*size,oy+(runtime.state.playerY+.5f)*size,size*.22f,paint)
  paint.color=Color.WHITE;paint.textSize=24f;c.drawText("25x25 TEST ROOM | "+runtime.stateHash().take(8),16f,30f,paint)
  c.drawText("CREDIT "+runtime.market.marketCredits+" | tile "+(room.tile(runtime.state.playerX,runtime.state.playerY)?.spaceCode?:"?"),16f,56f,paint)
 }
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_DOWN)return true;val dx=e.x-width/2f;val dy=e.y-height/2f;val cmd=if(kotlin.math.abs(dx)>kotlin.math.abs(dy)){if(dx<0)Command.LEFT else Command.RIGHT}else{if(dy<0)Command.UP else Command.DOWN};runtime.command(cmd);onCommand?.invoke(cmd);invalidate();return true}
}