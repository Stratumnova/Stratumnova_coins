package org.stratumnova.buildbycoin.world

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import java.io.File

object WorldEditor {
 fun show(activity:Activity,store:WorldStore,assets:List<File>,onBack:()->Unit,onTerminal:()->Unit,viewOnly:Boolean=false){
  val root=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setPadding(12,12,12,12);setBackgroundColor(Color.rgb(2,7,13))}
  fun button(t:String)=Button(activity).apply{text=t;isAllCaps=false}
  val nav=LinearLayout(activity)
  nav.addView(button("← Back").apply{setOnClickListener{onBack()}},LinearLayout.LayoutParams(0,-2,1f))
  nav.addView(button("World").apply{setOnClickListener{showWorld(activity,store,assets,onBack,onTerminal)}},LinearLayout.LayoutParams(0,-2,1f))
  nav.addView(button(if(viewOnly)"BUILD" else "VIEW").apply{setOnClickListener{show(activity,store,assets,onBack,onTerminal,!viewOnly)}},LinearLayout.LayoutParams(0,-2,1f))
  nav.addView(button(">_").apply{setOnClickListener{onTerminal()}},LinearLayout.LayoutParams(0,-2,1f));root.addView(nav)
  val cell=store.loadCell(store.activeId())
  root.addView(TextView(activity).apply{text=(if(viewOnly)"VIEW" else "BUILD")+"  •  "+cell.id+"  •  9×9";textSize=20f;setTextColor(Color.CYAN);setTypeface(typeface,Typeface.BOLD);setPadding(8,8,8,4)})
  val selected=TextView(activity).apply{text=if(viewOnly)"Game view — editor controls hidden." else "Touch to paint/select. Long press opens Inspector.";setTextColor(Color.LTGRAY);setPadding(8,2,8,6)};root.addView(selected)
  var brush=SpaceKind.FLOOR
  if(!viewOnly){
   val kinds=LinearLayout(activity)
   listOf(SpaceKind.FLOOR,SpaceKind.WALL,SpaceKind.PATH,SpaceKind.WATER,SpaceKind.OBJECT,SpaceKind.SPAWN,SpaceKind.EXIT).forEach{k->kinds.addView(button(k.name).apply{setOnClickListener{brush=k;selected.text="Brush: "+k.name}})}
   root.addView(HorizontalScrollView(activity).apply{addView(kinds)})
  }
  val grid=GridLayout(activity).apply{columnCount=9;rowCount=9;setBackgroundColor(Color.DKGRAY)}
  fun paint(b:Button,s:CellSpace){
   b.text=when{ s.label.isNotBlank()->s.label.take(2);s.assetPath.isNotBlank()->"▣";else->when(s.kind){SpaceKind.FLOOR->"·";SpaceKind.WALL->"■";SpaceKind.PATH->"═";SpaceKind.WATER->"≈";SpaceKind.OBJECT->"◆";SpaceKind.SPAWN->"S";SpaceKind.EXIT->"E"}}
   b.setTextColor(Color.WHITE);b.setBackgroundColor(when(s.kind){SpaceKind.FLOOR->Color.rgb(35,45,50);SpaceKind.WALL->Color.rgb(75,75,75);SpaceKind.PATH->Color.rgb(95,75,45);SpaceKind.WATER->Color.rgb(25,70,100);SpaceKind.OBJECT->Color.rgb(40,100,75);SpaceKind.SPAWN->Color.rgb(70,100,40);SpaceKind.EXIT->Color.rgb(100,60,40)})
  }
  cell.spaces.forEach{s->
   val b=button("").apply{
    gravity=Gravity.CENTER;textSize=12f;setPadding(0,0,0,0);paint(this,s)
    setOnClickListener{
     if(viewOnly){selected.text=cell.id+" / "+s.code+(if(s.label.isBlank())"" else " / "+s.label)}
     else{s.kind=brush;store.saveCell(cell);paint(this,s);selected.text=cell.id+" / "+s.code+" / "+s.kind.name}
    }
    setOnLongClickListener{if(viewOnly)false else{showInspector(activity,store,cell,s,assets){paint(this,s);selected.text=cell.id+" / "+s.code+" / "+s.kind.name};true}}
   }
   grid.addView(b,GridLayout.LayoutParams().apply{width=0;height=0;columnSpec=GridLayout.spec(s.x,1f);rowSpec=GridLayout.spec(s.y,1f);setMargins(1,1,1,1)})
  }
  root.addView(grid,LinearLayout.LayoutParams(-1,0,1f));activity.setContentView(root)
 }
 private fun showInspector(activity:Activity,store:WorldStore,cell:WorldCell,s:CellSpace,assets:List<File>,refresh:()->Unit){
  val box=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setPadding(20,8,20,8)}
  val label=EditText(activity).apply{hint="Label / object name";setText(s.label)}
  val target=EditText(activity).apply{hint="Exit target Cell ID";setText(s.targetCell)}
  box.addView(TextView(activity).apply{text=cell.id+" / "+s.code+"\nKind: "+s.kind.name})
  box.addView(label);box.addView(target)
  val assetNames=arrayOf("NONE")+assets.map{it.name}.toTypedArray()
  var chosen=s.assetPath
  box.addView(Button(activity).apply{text=if(chosen.isBlank())"ASSET: NONE" else "ASSET: "+File(chosen).name;setOnClickListener{
   AlertDialog.Builder(activity).setTitle("Place asset").setItems(assetNames){_,i->chosen=if(i==0)"" else assets[i-1].absolutePath;text=if(chosen.isBlank())"ASSET: NONE" else "ASSET: "+File(chosen).name}.show()
  }})
  AlertDialog.Builder(activity).setTitle("INSPECTOR").setView(box).setNegativeButton("Cancel",null).setPositiveButton("SAVE"){_,_->
   s.label=label.text.toString().trim();s.targetCell=target.text.toString().trim();s.assetPath=chosen
   if(s.assetPath.isNotBlank()&&s.kind==SpaceKind.FLOOR)s.kind=SpaceKind.OBJECT
   if(s.targetCell.isNotBlank())s.kind=SpaceKind.EXIT
   store.saveCell(cell);refresh()
  }.show()
 }
 private fun showWorld(activity:Activity,store:WorldStore,assets:List<File>,onBack:()->Unit,onTerminal:()->Unit){
  val root=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setPadding(12,12,12,12);setBackgroundColor(Color.rgb(2,7,13))}
  val top=LinearLayout(activity);top.addView(Button(activity).apply{text="← Cell";setOnClickListener{show(activity,store,assets,onBack,onTerminal)}},LinearLayout.LayoutParams(0,-2,1f));top.addView(Button(activity).apply{text=">_ Terminal";setOnClickListener{onTerminal()}},LinearLayout.LayoutParams(0,-2,1f));root.addView(top)
  root.addView(TextView(activity).apply{text="WORLD MAP";textSize=22f;setTextColor(Color.CYAN);setPadding(8,12,8,8)})
  root.addView(TextView(activity).apply{text="Each block is one 9×9 Cell. Tap a Cell to edit it.";setTextColor(Color.LTGRAY);setPadding(8,0,8,12)})
  store.cells().forEach{j->root.addView(Button(activity).apply{text=j.optString("id")+"  •  "+j.optString("name")+"\nWorld ["+j.optInt("x")+","+j.optInt("y")+"]";setOnClickListener{store.setActive(j.optString("id"));show(activity,store,assets,onBack,onTerminal)}})}
  root.addView(Button(activity).apply{text="+ ADD 9×9 CELL";setOnClickListener{store.addCell();showWorld(activity,store,assets,onBack,onTerminal)}})
  activity.setContentView(ScrollView(activity).apply{addView(root)})
 }
}