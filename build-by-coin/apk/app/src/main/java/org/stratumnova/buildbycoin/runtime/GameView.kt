package org.stratumnova.buildbycoin.runtime
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
class GameView(context:Context,val runtime:RuntimeCore=RuntimeCore()):View(context){
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
 override fun onDraw(c:Canvas){super.onDraw(c);c.drawColor(Color.rgb(7,13,18));val size=Math.min(width,height)*.16f;val ox=(width-size*5)/2f;val oy=(height-size*5)/2f
 for(y in 0..4)for(x in 0..4){paint.style=Paint.Style.FILL;paint.color=if(x==0||y==0||x==4||y==4)Color.rgb(48,58,63) else Color.rgb(83,67,45);c.drawRect(ox+x*size,oy+y*size,ox+(x+1)*size-2,oy+(y+1)*size-2,paint)}
 paint.color=Color.rgb(0,213,255);c.drawCircle(ox+(runtime.state.playerX+.5f)*size,oy+(runtime.state.playerY+.5f)*size,size*.28f,paint);paint.color=Color.WHITE;paint.textSize=28f;c.drawText("tick "+runtime.clock.tick+"   "+runtime.stateHash().take(8),20f,40f,paint)}
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_DOWN)return true;val dx=e.x-width/2f;val dy=e.y-height/2f;runtime.command(if(kotlin.math.abs(dx)>kotlin.math.abs(dy)){if(dx<0)Command.LEFT else Command.RIGHT}else{if(dy<0)Command.UP else Command.DOWN});invalidate();return true}
}