package org.stratumnova.buildbycoin.assets
import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.view.ViewGroup
import android.widget.*
import java.io.File

object AssetPreview {
 private val bg=Color.rgb(2,7,13)
 private val panel=Color.rgb(5,18,29)
 private val cyan=Color.rgb(0,190,255)
 private fun text(a:Activity,t:String,size:Float=14f)=TextView(a).apply{text=t;textSize=size;setTextColor(Color.WHITE);setPadding(10,8,10,8)}
 private fun root(a:Activity,onBack:()->Unit)=LinearLayout(a).apply{
  orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(bg)
  addView(Button(a).apply{text="← ASSETS";setOnClickListener{onBack()}})
 }
 fun showImage(a:Activity,file:File,onBack:()->Unit){
  val bitmap=BitmapFactory.decodeFile(file.path)
  val box=root(a,onBack)
  box.addView(text(a,file.name,20f).apply{setTextColor(cyan)})
  box.addView(text(a,(bitmap?.width?:0).toString()+" × "+(bitmap?.height?:0)+"   "+file.extension.uppercase()+"   "+file.length()+" B"))
  val image=ImageView(a).apply{setImageBitmap(bitmap);adjustViewBounds=true;scaleType=ImageView.ScaleType.FIT_CENTER;setBackgroundColor(panel)}
  box.addView(image,LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f))
  val scaleRow=LinearLayout(a)
  listOf("FIT" to 1f,"1×" to 1f,"2×" to 2f,"4×" to 4f,"8×" to 8f,"16×" to 16f).forEach{(name,x)->
   scaleRow.addView(Button(a).apply{text=name;setOnClickListener{image.scaleX=x;image.scaleY=x}},LinearLayout.LayoutParams(0,-2,1f))
  }
  box.addView(scaleRow)
  val seek=SeekBar(a).apply{max=1575;progress=75}
  val value=text(a,"Scale 1.00×")
  seek.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
   override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){val x=(p.coerceAtLeast(25))/100f;image.scaleX=x;image.scaleY=x;value.text="Scale %.2f×".format(x)}
   override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){}
  })
  box.addView(value);box.addView(seek)
  box.addView(text(a,"Pixel preview • source file remains unchanged").apply{setTextColor(cyan)})
  a.setContentView(box)
 }
 fun showAudio(a:Activity,file:File,onBack:()->Unit){
  val box=root(a,onBack)
  box.addView(text(a,file.name,20f).apply{setTextColor(cyan)})
  val player=MediaPlayer().apply{setDataSource(file.path);prepare()}
  box.addView(text(a,file.extension.uppercase()+"   "+file.length()+" B   "+player.duration+" ms"))
  val seek=SeekBar(a).apply{max=player.duration}
  val controls=LinearLayout(a)
  controls.addView(Button(a).apply{text="PLAY / PAUSE";setOnClickListener{if(player.isPlaying)player.pause() else player.start()}},LinearLayout.LayoutParams(0,-2,2f))
  val loop=CheckBox(a).apply{text="LOOP";setTextColor(Color.WHITE);setOnCheckedChangeListener{_,v->player.isLooping=v}}
  controls.addView(loop,LinearLayout.LayoutParams(0,-2,1f))
  seek.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
   override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){if(u)player.seekTo(p)}
   override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){}
  })
  box.addView(text(a,"AUDIO PREVIEW").apply{setTextColor(cyan)});box.addView(seek);box.addView(controls)
  box.addView(text(a,"Volume"))
  box.addView(SeekBar(a).apply{max=100;progress=100;setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
   override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){val v=p/100f;player.setVolume(v,v)}
   override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){}
  })})
  box.addView(text(a,"Used by: UNRESOLVED"))
  a.setContentView(box)
 }
}