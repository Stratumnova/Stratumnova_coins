package org.stratumnova.buildbycoin.world
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import java.io.File
class WorldRunView(context:Context,val store:WorldStore):View(context){
 val session=WorldSession(store);private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val cache=mutableMapOf<String,Bitmap?>();var onState:((String)->Unit)?=null
 override fun onDraw(c:Canvas){super.onDraw(c);c.drawColor(Color.rgb(7,13,18));val cell=store.loadCell(session.cellId);val size=minOf(width/9f,height/9f);val ox=(width-size*9)/2f;val oy=(height-size*9)/2f
  cell.spaces.forEach{s->val l=ox+s.x*size;val t=oy+s.y*size;p.style=Paint.Style.FILL;p.color=when(s.kind){SpaceKind.WALL->Color.rgb(75,75,75);SpaceKind.PATH->Color.rgb(105,80,45);SpaceKind.WATER->Color.rgb(25,75,110);SpaceKind.OBJECT->Color.rgb(45,105,75);SpaceKind.SPAWN->Color.rgb(65,100,45);SpaceKind.EXIT->Color.rgb(120,70,35);else->Color.rgb(38,48,53)};c.drawRect(l,t,l+size-1,t+size-1,p)
   if(s.assetPath.isNotBlank()){val b=cache.getOrPut(s.assetPath){runCatching{BitmapFactory.decodeFile(s.assetPath)}.getOrNull()};if(b!=null)c.drawBitmap(b,null,RectF(l,t,l+size,t+size),p)}
  }
  p.color=Color.CYAN;c.drawCircle(ox+(session.x+.5f)*size,oy+(session.y+.5f)*size,size*.24f,p);p.color=Color.WHITE;p.textSize=22f;c.drawText(session.cellId+"  ["+session.x+","+session.y+"]",12f,28f,p)
 }
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_DOWN)return true;val dx=e.x-width/2f;val dy=e.y-height/2f;if(kotlin.math.abs(dx)>kotlin.math.abs(dy))session.move(if(dx<0)-1 else 1,0) else session.move(0,if(dy<0)-1 else 1);invalidate();onState?.invoke(session.cellId+" / "+session.x+","+session.y);return true}
}