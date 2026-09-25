package org.stratumnova.buildbycoin.world
import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.widget.*
object MapCorrectionEditor{
 fun show(a:Activity,d:MapDraft,store:WorldStore,onDone:()->Unit){
  val root=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setPadding(10,10,10,10);setBackgroundColor(Color.rgb(2,7,13))}
  val title=TextView(a).apply{text="MAP CONVERSION • CORRECT BEFORE INTAKE";textSize=19f;setTextColor(Color.CYAN)};root.addView(title)
  val info=TextView(a).apply{text="Paint converted tiles to fix edges or missed squares. Nothing enters the World until APPROVE.";setTextColor(Color.LTGRAY)};root.addView(info)
  var brush=SpaceKind.FLOOR
  val tools=LinearLayout(a);SpaceKind.values().filter{it!=SpaceKind.SPAWN&&it!=SpaceKind.EXIT}.forEach{k->tools.addView(Button(a).apply{text=k.name;isAllCaps=false;setOnClickListener{brush=k}})};root.addView(HorizontalScrollView(a).apply{addView(tools)})
  val grid=GridLayout(a).apply{columnCount=d.width;rowCount=d.height}
  fun paint(b:Button,t:MapTile){b.text=when(t.kind){SpaceKind.WALL->"■";SpaceKind.PATH->"═";SpaceKind.WATER->"≈";SpaceKind.OBJECT->"◆";else->"·"};b.setTextColor(Color.WHITE);b.setBackgroundColor(when(t.kind){SpaceKind.WALL->Color.DKGRAY;SpaceKind.PATH->Color.rgb(180,55,110);SpaceKind.WATER->Color.rgb(55,125,190);SpaceKind.OBJECT->Color.rgb(145,85,50);else->Color.rgb(115,145,55)})}
  for(y in 0 until d.height)for(x in 0 until d.width){val t=d.tile(x,y);val b=Button(a).apply{gravity=Gravity.CENTER;textSize=9f;setPadding(0,0,0,0);paint(this,t);setOnClickListener{t.kind=brush;paint(this,t)}};grid.addView(b,GridLayout.LayoutParams().apply{width=54;height=54;columnSpec=GridLayout.spec(x);rowSpec=GridLayout.spec(y)})}
  val scroller=HorizontalScrollView(a);scroller.addView(ScrollView(a).apply{addView(grid)});root.addView(scroller,LinearLayout.LayoutParams(-1,0,1f))
  val actions=LinearLayout(a);actions.addView(Button(a).apply{text="CANCEL";setOnClickListener{onDone()}},LinearLayout.LayoutParams(0,-2,1f));actions.addView(Button(a).apply{text="APPROVE → 9×9 CELLS";setOnClickListener{MapImporter.commit(d,store);onDone()}},LinearLayout.LayoutParams(0,-2,2f));root.addView(actions);a.setContentView(root)
 }
}