package org.stratumnova.buildbycoin.assets
import android.app.Activity
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.view.ViewGroup
import android.widget.*
import java.io.File
object AssetPreview {
 fun showImage(a:Activity,file:File){
  val box=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL}
  val image=ImageView(a).apply{setImageBitmap(BitmapFactory.decodeFile(file.path));adjustViewBounds=true;scaleType=ImageView.ScaleType.FIT_CENTER}
  val seek=SeekBar(a).apply{max=1500;progress=100}
  val value=TextView(a).apply{text="Scale 1.0x"}
  seek.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){val x=(p.coerceAtLeast(25))/100f;image.scaleX=x;image.scaleY=x;value.text="Scale %.2fx".format(x)};override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){}})
  box.addView(image,LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f));box.addView(value);box.addView(seek);a.setContentView(box)
 }
 fun showAudio(a:Activity,file:File){
  val box=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL};val title=TextView(a).apply{text=file.name;textSize=20f};val play=Button(a).apply{text="PLAY / PAUSE"};val seek=SeekBar(a)
  val player=MediaPlayer().apply{setDataSource(file.path);prepare()};seek.max=player.duration
  play.setOnClickListener{if(player.isPlaying)player.pause() else player.start()}
  seek.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){if(u)player.seekTo(p)};override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){}})
  box.addView(title);box.addView(play);box.addView(seek);a.setContentView(box)
 }
}