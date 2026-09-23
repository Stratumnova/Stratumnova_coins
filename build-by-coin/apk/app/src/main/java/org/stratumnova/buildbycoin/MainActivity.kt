package org.stratumnova.buildbycoin
import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.content.Intent
import android.widget.*
import org.stratumnova.buildbycoin.intake.IntakeScanner
import org.stratumnova.buildbycoin.workspace.WorkspaceStore
import org.stratumnova.buildbycoin.coin.CoinInspector
import org.stratumnova.buildbycoin.runtime.GameView
import org.stratumnova.buildbycoin.test.TestPanel
import org.stratumnova.buildbycoin.export.Exporter
import java.io.File
class MainActivity:Activity(){
 private lateinit var terminal:TextView;private lateinit var store:WorkspaceStore
 private val OPEN_FILE=1001;private val OPEN_ZIP=1002;private val OPEN_ASSET=1003;private val SAVE_ZIP=2001;private val SAVE_FILE=2002;private var pendingExport:File?=null
 override fun onCreate(state:Bundle?){super.onCreate(state);store=WorkspaceStore(this);val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
 fun label(t:String,s:Float=16f)=TextView(this).apply{text=t;textSize=s;setTextColor(Color.WHITE);setPadding(8,8,8,8)}
 root.addView(label("BUILD BY COIN",24f));root.addView(label("COIN TESTING & GAME BUILDER",12f));val tabs=LinearLayout(this)
 listOf("Terminal","Files","Assets","Build","Run","Export").forEach{n->tabs.addView(Button(this).apply{text=n;setOnClickListener{if(n=="Files")showFiles() else if(n=="Run")TestPanel.show(this@MainActivity) else if(n=="Export")showExport()}})};root.addView(tabs)
 terminal=label("> Welcome to Build by Coin\n> Ready for intake\n> Runtime: v0.1.0\n> Originals remain untouched.",15f);root.addView(terminal,LinearLayout.LayoutParams(-1,0,1f));root.addView(label("INTAKE",18f));val intake=LinearLayout(this)
 fun b(n:String,c:Int,t:String):Button { return Button(this).apply { text=n; setOnClickListener { pick(c,t) } } }
 intake.addView(b("FILE",OPEN_FILE,"*/*"),LinearLayout.LayoutParams(0,-2,1f))
 intake.addView(b("ZIP",OPEN_ZIP,"application/zip"),LinearLayout.LayoutParams(0,-2,1f))
 intake.addView(b("ASSET",OPEN_ASSET,"*/*"),LinearLayout.LayoutParams(0,-2,1f))
 root.addView(intake)
 root.addView(label("Single files for quick intake; ZIP for cartridges and large sets.",14f))
 setContentView(root)
 }
 private fun pick(c:Int,t:String)=startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type=t},c)
 override fun onActivityResult(r:Int,rc:Int,data:Intent?){super.onActivityResult(r,rc,data);if(rc!=RESULT_OK)return;val uri=data?.data?:return;try{if(r==SAVE_ZIP){Exporter.projectZip(this,File(filesDir,"workspace"),uri);terminal.text="> PROJECT ZIP EXPORTED";return};if(r==SAVE_FILE){pendingExport?.let{Exporter.singleFile(this,it,uri)};terminal.text="> SINGLE FILE EXPORTED";return};val scanner=IntakeScanner(this);if(r==OPEN_ZIP){val items=scanner.scanZip(uri);val groups=items.groupingBy{it.kind}.eachCount();val cartridge=store.importZip(uri);terminal.text="> ZIP scanned\n> Entries: "+items.size+"\n"+groups.entries.sortedByDescending{it.value}.joinToString("\n"){"> "+it.key+": "+it.value}+"\n> Cartridge: "+cartridge.root.name+"\n> Extracted files: "+cartridge.files.size+"\n> Source ZIP not modified."}else{val e=scanner.scanSingle(uri);val saved=store.preserveSingle(uri,e);val c=CoinInspector.inspect(saved.file);terminal.text=if(c!=null)"> COIN PRESERVED\n> "+c.coinId+"\n> "+c.name+"\n> "+c.category+" / "+c.domain+"\n> Role: "+c.role+"\n> READ ONLY\n> SHA-256: "+e.sha256 else "> FILE PRESERVED\n> "+e.name+"\n> Type: "+e.kind+"\n> Bytes: "+e.bytes+"\n> SHA-256: "+e.sha256+"\n> Candidate coin fields: UNRESOLVED"}}catch(e:Exception){terminal.text="> INTAKE ERROR\n> "+e.javaClass.simpleName+": "+e.message}}
 private fun showExport(){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};box.addView(Button(this).apply{text="PROJECT ZIP";setOnClickListener{startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/zip";putExtra(Intent.EXTRA_TITLE,"BuildByCoin_Project.zip")},SAVE_ZIP)}});val files=store.listOriginals();files.take(30).forEach{file->box.addView(Button(this).apply{text="FILE: "+file.name;setOnClickListener{pendingExport=file;startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/octet-stream";putExtra(Intent.EXTRA_TITLE,file.name)},SAVE_FILE)}})};setContentView(ScrollView(this).apply{addView(box)})}
 private fun showFiles(){val fs=store.listOriginals();terminal.text=if(fs.isEmpty())"> FILES\n> No preserved originals." else "> FILES ("+fs.size+")\n"+fs.take(40).joinToString("\n"){"> "+it.name+"  "+it.length()+" B"}+(if(fs.size>40)"\n> ... "+(fs.size-40)+" more" else "")}
}