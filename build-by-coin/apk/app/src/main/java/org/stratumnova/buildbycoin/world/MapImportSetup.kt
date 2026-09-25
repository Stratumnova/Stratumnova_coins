package org.stratumnova.buildbycoin.world
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.*
object MapImportSetup{
 fun show(a:Activity,bitmap:Bitmap,store:WorldStore,onCancel:()->Unit){
  val root=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setPadding(18,18,18,18);setBackgroundColor(Color.rgb(2,7,13))}
  root.addView(TextView(a).apply{text="MAP IMPORT";textSize=22f;setTextColor(Color.CYAN)})
  root.addView(TextView(a).apply{text="Set the coded map size. The image is sampled into this grid, then opens the correction editor before any World data is replaced.";setTextColor(Color.LTGRAY);setPadding(0,8,0,14)})
  val cols=EditText(a).apply{hint="Columns";setText("32");inputType=2;setTextColor(Color.WHITE)}
  val rows=EditText(a).apply{hint="Rows";setText("32");inputType=2;setTextColor(Color.WHITE)}
  root.addView(cols);root.addView(rows)
  val preview=ImageView(a).apply{setImageBitmap(bitmap);adjustViewBounds=true;scaleType=ImageView.ScaleType.FIT_CENTER};root.addView(preview,LinearLayout.LayoutParams(-1,0,1f))
  val actions=LinearLayout(a)
  actions.addView(Button(a).apply{text="CANCEL";setOnClickListener{onCancel()}},LinearLayout.LayoutParams(0,-2,1f))
  actions.addView(Button(a).apply{text="CONVERT → CORRECT";setOnClickListener{val w=cols.text.toString().toIntOrNull()?.coerceIn(1,128)?:32;val h=rows.text.toString().toIntOrNull()?.coerceIn(1,128)?:32;val draft=MapImporter.fromBitmap(bitmap,w,h);MapCorrectionEditor.show(a,draft,store,onCancel)}},LinearLayout.LayoutParams(0,-2,2f))
  root.addView(actions);a.setContentView(root)
 }
}