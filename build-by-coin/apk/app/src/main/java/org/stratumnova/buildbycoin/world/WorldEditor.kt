package org.stratumnova.buildbycoin.world

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*

object WorldEditor {
 fun show(activity:Activity,store:WorldStore,onBack:()->Unit,onTerminal:()->Unit){
  val root=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setPadding(12,12,12,12);setBackgroundColor(Color.rgb(2,7,13))}
  val nav=LinearLayout(activity)
  fun button(t:String)=Button(activity).apply{text=t;isAllCaps=false}
  nav.addView(button("← Back").apply{setOnClickListener{onBack()}},LinearLayout.LayoutParams(0,-2,1f))
  nav.addView(button("World").apply{setOnClickListener{showWorld(activity,store,onBack,onTerminal)}},LinearLayout.LayoutParams(0,-2,1f))
  nav.addView(button(">_ Terminal").apply{setOnClickListener{onTerminal()}},LinearLayout.LayoutParams(0,-2,1f));root.addView(nav)
  val cell=store.loadCell(store.activeId())
  root.addView(TextView(activity).apply{text="BUILD  •  "+cell.id+"  •  9×9 CELL";textSize=20f;setTextColor(Color.CYAN);setTypeface(typeface,Typeface.BOLD);setPadding(8,10,8,6)})
  root.addView(TextView(activity).apply{text="Touch a space to select/place. Long press to type a label. This cell is a complete tiny-game unit.";setTextColor(Color.LTGRAY);setPadding(8,0,8,8)})
  val selected=TextView(activity).apply{text="Selected: none";setTextColor(Color.WHITE);setPadding(8,4,8,8)};root.addView(selected)
  val kinds=LinearLayout(activity).apply{orientation=LinearLayout.HORIZONTAL};var brush=SpaceKind.FLOOR
  listOf(SpaceKind.FLOOR,SpaceKind.WALL,SpaceKind.PATH,SpaceKind.WATER,SpaceKind.OBJECT).forEach{k->kinds.addView(button(k.name).apply{setOnClickListener{brush=k;selected.text="Brush: "+k.name}},LinearLayout.LayoutParams(0,-2,1f))}
  root.addView(HorizontalScrollView(activity).apply{addView(kinds)})
  val grid=GridLayout(activity).apply{columnCount=9;rowCount=9;setBackgroundColor(Color.DKGRAY)}
  fun paint(b:Button,s:CellSpace){b.text=if(s.label.isBlank())when(s.kind){SpaceKind.FLOOR->"·";SpaceKind.WALL->"■";SpaceKind.PATH->"═";SpaceKind.WATER->"≈";SpaceKind.OBJECT->"◆";SpaceKind.SPAWN->"S";SpaceKind.EXIT->"E"} else s.label.take(2);b.setTextColor(Color.WHITE);b.setBackgroundColor(when(s.kind){SpaceKind.FLOOR->Color.rgb(35,45,50);SpaceKind.WALL->Color.rgb(75,75,75);SpaceKind.PATH->Color.rgb(95,75,45);SpaceKind.WATER->Color.rgb(25,70,100);SpaceKind.OBJECT->Color.rgb(40,100,75);SpaceKind.SPAWN->Color.rgb(70,100,40);SpaceKind.EXIT->Color.rgb(100,60,40)})}
  cell.spaces.forEach{s->val b=button("").apply{gravity=Gravity.CENTER;textSize=12f;setPadding(0,0,0,0);paint(this,s);setOnClickListener{s.kind=brush;store.saveCell(cell);paint(this,s);selected.text=cell.id+" / "+s.code+" / "+s.kind.name+(if(s.label.isBlank())"" else " / "+s.label)};setOnLongClickListener{val input=EditText(activity).apply{setText(s.label);hint="Object / tile label"};AlertDialog.Builder(activity).setTitle(cell.id+" / "+s.code).setView(input).setNegativeButton("Cancel",null).setPositiveButton("Save"){_,_->s.label=input.text.toString().trim();if(s.label.isNotBlank()&&s.kind==SpaceKind.FLOOR)s.kind=SpaceKind.OBJECT;store.saveCell(cell);paint(this,s);selected.text=cell.id+" / "+s.code+" / "+s.kind.name+" / "+s.label}.show();true}}
   grid.addView(b,GridLayout.LayoutParams().apply{width=0;height=0;columnSpec=GridLayout.spec(s.x,1f);rowSpec=GridLayout.spec(s.y,1f);setMargins(1,1,1,1)})
  }
  root.addView(grid,LinearLayout.LayoutParams(-1,0,1f));activity.setContentView(root)
 }
 private fun showWorld(activity:Activity,store:WorldStore,onBack:()->Unit,onTerminal:()->Unit){
  val root=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setPadding(12,12,12,12);setBackgroundColor(Color.rgb(2,7,13))}
  val top=LinearLayout(activity);top.addView(Button(activity).apply{text="← Cell";setOnClickListener{show(activity,store,onBack,onTerminal)}},LinearLayout.LayoutParams(0,-2,1f));top.addView(Button(activity).apply{text=">_ Terminal";setOnClickListener{onTerminal()}},LinearLayout.LayoutParams(0,-2,1f));root.addView(top)
  root.addView(TextView(activity).apply{text="WORLD MAP";textSize=22f;setTextColor(Color.CYAN);setPadding(8,12,8,8)})
  root.addView(TextView(activity).apply{text="Each block is one 9×9 Cell. 1 Cell can be a complete tiny game; add Cells to grow the world.";setTextColor(Color.LTGRAY);setPadding(8,0,8,12)})
  store.cells().forEach{j->root.addView(Button(activity).apply{text=j.optString("id")+"  •  "+j.optString("name")+"\nWorld ["+j.optInt("x")+","+j.optInt("y")+"]";setOnClickListener{store.setActive(j.optString("id"));show(activity,store,onBack,onTerminal)}})}
  root.addView(Button(activity).apply{text="+ ADD 9×9 CELL";setOnClickListener{store.addCell();showWorld(activity,store,onBack,onTerminal)}})
  activity.setContentView(ScrollView(activity).apply{addView(root)})
 }
}