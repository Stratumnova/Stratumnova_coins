package org.stratumnova.buildbycoin.world

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.text.InputType
import android.widget.*

object MapImportSetup {
 fun show(a:Activity, bitmap:Bitmap, store:WorldStore, workspaceRoot:java.io.File, onCancel:()->Unit) {
  val root=LinearLayout(a).apply {
   orientation=LinearLayout.VERTICAL
   setPadding(18,18,18,18)
   setBackgroundColor(Color.rgb(2,7,13))
  }
  root.addView(TextView(a).apply{text="MAP IMPORT";textSize=22f;setTextColor(Color.CYAN)})
  root.addView(TextView(a).apply{
   text="Set grid size, crop/alignment, and color mapping. Conversion opens the correction editor before World data changes."
   setTextColor(Color.LTGRAY);setPadding(0,8,0,14)
  })
  val cols=EditText(a).apply{hint="Columns";setText("32");inputType=InputType.TYPE_CLASS_NUMBER;setTextColor(Color.WHITE)}
  val rows=EditText(a).apply{hint="Rows";setText("32");inputType=InputType.TYPE_CLASS_NUMBER;setTextColor(Color.WHITE)}
  val crop=EditText(a).apply{hint="Crop L,T,R,B %";setText("0,0,100,100");setTextColor(Color.WHITE)}
  root.addView(cols);root.addView(rows);root.addView(crop)

  val legend=MapImporter.defaultLegend()
  root.addView(Button(a).apply{
   text="COLOR LEGEND"
   setOnClickListener{
    val labels=legend.mapIndexed{i,e->"Mapping "+(i+1)+" → "+e.kind.name}.toTypedArray()
    AlertDialog.Builder(a).setTitle("Color mappings").setItems(labels){_,index->
     val kinds=SpaceKind.values().map{it.name}.toTypedArray()
     AlertDialog.Builder(a).setTitle(labels[index]).setItems(kinds){_,kindIndex->
      legend[index].kind=SpaceKind.values()[kindIndex]
     }.show()
    }.show()
   }
  })

  root.addView(ImageView(a).apply{setImageBitmap(bitmap);adjustViewBounds=true;scaleType=ImageView.ScaleType.FIT_CENTER},LinearLayout.LayoutParams(-1,0,1f))
  val actions=LinearLayout(a)
  actions.addView(Button(a).apply{text="CANCEL";setOnClickListener{onCancel()}},LinearLayout.LayoutParams(0,-2,1f))
  actions.addView(Button(a).apply{
   text="CONVERT → CORRECT"
   setOnClickListener{
    val w=cols.text.toString().toIntOrNull()?.coerceIn(1,128)?:32
    val h=rows.text.toString().toIntOrNull()?.coerceIn(1,128)?:32
    val q=crop.text.toString().split(",").mapNotNull{it.trim().toFloatOrNull()?.div(100f)}
    val v=if(q.size==4) q else listOf(0f,0f,1f,1f)
    val draft=MapImporter.fromBitmap(bitmap,w,h,legend,v[0],v[1],v[2],v[3])
    MapCorrectionEditor.show(a,draft,store){approved->
     if(approved) MapArchive.preserve(workspaceRoot,bitmap,draft,legend,v)
     onCancel()
    }
   }
  },LinearLayout.LayoutParams(0,-2,2f))
  root.addView(actions)
  a.setContentView(root)
 }
}
