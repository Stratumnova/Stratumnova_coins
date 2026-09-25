package org.stratumnova.buildbycoin.world
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

class WorldLayoutView(context:Context,val store:WorldStore):View(context){
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG);var onCellOpen:((String)->Unit)?=null;var onChanged:(()->Unit)?=null
 private var dragId:String?=null;private val size=150f;private val gap=24f
 private fun originX()=width/2f-size/2f
 private fun originY()=height/2f-size/2f
 private fun rect(x:Int,y:Int)=RectF(originX()+x*(size+gap),originY()+y*(size+gap),originX()+x*(size+gap)+size,originY()+y*(size+gap)+size)
 override fun onDraw(c:Canvas){super.onDraw(c);c.drawColor(Color.rgb(7,13,18));paint.textAlign=Paint.Align.CENTER
  store.cells().forEach{j->val r=rect(j.optInt("x"),j.optInt("y"));paint.style=Paint.Style.FILL;paint.color=if(j.optString("id")==store.activeId())Color.rgb(20,95,115) else Color.rgb(35,48,56);c.drawRoundRect(r,12f,12f,paint);paint.style=Paint.Style.STROKE;paint.strokeWidth=3f;paint.color=Color.rgb(0,213,255);c.drawRoundRect(r,12f,12f,paint);paint.style=Paint.Style.FILL;paint.color=Color.WHITE;paint.textSize=22f;c.drawText(j.optString("id"),r.centerX(),r.centerY()-8f,paint);paint.textSize=16f;c.drawText("["+j.optInt("x")+","+j.optInt("y")+"]",r.centerX(),r.centerY()+22f,paint)}
 }
 override fun onTouchEvent(e:MotionEvent):Boolean{
  when(e.action){
   MotionEvent.ACTION_DOWN->{val hit=store.cells().firstOrNull{rect(it.optInt("x"),it.optInt("y")).contains(e.x,e.y)}?:return true;dragId=hit.optString("id");store.setActive(dragId!!);invalidate()}
   MotionEvent.ACTION_UP->{val id=dragId?:return true;val gx=((e.x-originX())/(size+gap)).roundToInt();val gy=((e.y-originY())/(size+gap)).roundToInt();val occupied=store.cellAt(gx,gy);if(occupied==null||occupied==id){val before=store.cells().firstOrNull{it.optString("id")==id};val moved=before!=null&&(before.optInt("x")!=gx||before.optInt("y")!=gy);if(moved){store.moveCell(id,gx,gy);store.connectAdjacent();onChanged?.invoke()}else onCellOpen?.invoke(id)};dragId=null;invalidate()}
  };return true
 }
}