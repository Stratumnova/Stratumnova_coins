package org.stratumnova.buildbycoin
import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.widget.*
import org.stratumnova.buildbycoin.intake.IntakeScanner
import org.stratumnova.buildbycoin.workspace.WorkspaceStore
import org.stratumnova.buildbycoin.coin.CoinInspector
import org.stratumnova.buildbycoin.runtime.GameView
import org.stratumnova.buildbycoin.test.TestPanel
import org.stratumnova.buildbycoin.export.Exporter
import org.stratumnova.buildbycoin.assets.AssetBrowser
import org.stratumnova.buildbycoin.assets.AssetPreview
import java.io.File
class MainActivity:Activity(){
 private lateinit var terminal:TextView;private lateinit var store:WorkspaceStore;private var mainView:android.view.View?=null;private var lastCartridge:org.stratumnova.buildbycoin.workspace.CartridgeResult?=null
 private val OPEN_FILE=1001;private val OPEN_ZIP=1002;private val OPEN_ASSET=1003;private val SAVE_ZIP=2001;private val SAVE_FILE=2002;private var pendingExport:File?=null
 override fun onCreate(state:Bundle?){super.onCreate(state);store=WorkspaceStore(this);val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
 val cyan=Color.rgb(0,190,255);val panel=Color.rgb(5,18,29)
 fun panelBg(stroke:Boolean=true)=GradientDrawable().apply{setColor(panel);cornerRadius=12f;if(stroke)setStroke(2,cyan)}
 fun label(t:String,s:Float=16f)=TextView(this).apply{text=t;textSize=s;setTextColor(Color.WHITE);setPadding(8,8,8,8)}
 fun tab(n:String)=Button(this).apply{text=n;textSize=12f;setTextColor(Color.WHITE);background=panelBg();setPadding(4,2,4,2)}
 root.addView(label("◈  BUILD BY COIN",24f).apply{setTextColor(cyan);setTypeface(typeface,Typeface.BOLD)});root.addView(label("COIN TESTING & GAME BUILDER",12f).apply{setTextColor(cyan)});val tabs=LinearLayout(this)
 listOf("Terminal","Files","Assets","Build","Run","Export").forEach { n ->
  val button=tab(n).apply { setOnClickListener {
   when(n){"Terminal"->showMain();"Files"->showFiles();"Assets"->showAssets();"Build"->showBuild();"Run"->TestPanel.show(this@MainActivity){showMain()};"Export"->showExport()}
  }}
  tabs.addView(button,LinearLayout.LayoutParams(0,56,1f))
 }
 root.addView(tabs)
 terminal=label("> Welcome to Build by Coin\n> Ready for intake\n> Tap FILE, ZIP or ASSET\n> Runtime: v0.1.0\n> Originals remain untouched.",15f).apply{setTextColor(Color.rgb(0,255,150));typeface=Typeface.MONOSPACE;background=panelBg()};root.addView(terminal,LinearLayout.LayoutParams(-1,0,1f));root.addView(label("INTAKE",18f));val intake=LinearLayout(this)
 fun b(n:String,c:Int,t:String):Button { return Button(this).apply { text=n; setOnClickListener { pick(c,t) } } }
 intake.addView(b("FILE",OPEN_FILE,"*/*"),LinearLayout.LayoutParams(0,-2,1f))
 intake.addView(b("ZIP",OPEN_ZIP,"application/zip"),LinearLayout.LayoutParams(0,-2,1f))
 intake.addView(b("ASSET",OPEN_ASSET,"*/*"),LinearLayout.LayoutParams(0,-2,1f))
 root.addView(intake)
 root.addView(label("Single files for quick intake; ZIP for cartridges and large sets.",14f))
 mainView=root;setContentView(root)
 }
 private fun pick(c:Int,t:String)=startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type=t},c)
 override fun onActivityResult(r:Int,rc:Int,data:Intent?){super.onActivityResult(r,rc,data);if(rc!=RESULT_OK)return;val uri=data?.data?:return;try{if(r==SAVE_ZIP){Exporter.projectZip(this,File(filesDir,"workspace"),uri);terminal.text="> PROJECT ZIP EXPORTED";return};if(r==SAVE_FILE){pendingExport?.let{Exporter.singleFile(this,it,uri)};terminal.text="> SINGLE FILE EXPORTED";return};val scanner=IntakeScanner(this);if(r==OPEN_ZIP){val items=scanner.scanZip(uri);val groups=items.groupingBy{it.kind}.eachCount();val cartridge=store.importZip(uri);lastCartridge=cartridge;terminal.text="> ZIP scanned\n> Entries: "+items.size+"\n"+groups.entries.sortedByDescending{it.value}.joinToString("\n"){"> "+it.key+": "+it.value}+"\n> Cartridge: "+cartridge.root.name+"\n> Extracted files: "+cartridge.files.size+"\n> Source ZIP not modified."}else{val e=scanner.scanSingle(uri);val saved=store.preserveSingle(uri,e);val c=CoinInspector.inspect(saved.file);terminal.text=if(c!=null)"> COIN PRESERVED\n> "+c.coinId+"\n> "+c.name+"\n> "+c.category+" / "+c.domain+"\n> Role: "+c.role+"\n> READ ONLY\n> SHA-256: "+e.sha256 else "> FILE PRESERVED\n> "+e.name+"\n> Type: "+e.kind+"\n> Bytes: "+e.bytes+"\n> SHA-256: "+e.sha256+"\n> Candidate coin fields: UNRESOLVED"}}catch(e:Exception){terminal.text="> INTAKE ERROR\n> "+e.javaClass.simpleName+": "+e.message}}
 private fun showMain(){mainView?.let{setContentView(it)}}
 override fun onBackPressed(){if(mainView!=null && mainView?.parent==null)showMain() else super.onBackPressed()}
 private fun workingFiles():List<File>{val w=File(filesDir,"workspace/working");return if(w.exists())w.walkTopDown().filter{it.isFile}.toList() else emptyList()}
 private fun showAssets(){val assets=AssetBrowser.index(workingFiles());val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16)};box.addView(Button(this).apply{text="← BACK";setOnClickListener{showMain()}});box.addView(TextView(this).apply{text="ASSETS ("+assets.size+")";textSize=20f});assets.take(200).forEach{a->box.addView(Button(this).apply{text=a.kind+"  "+a.file.name;setOnClickListener{if(a.kind=="IMAGE")AssetPreview.showImage(this@MainActivity,a.file){showAssets()} else AssetPreview.showAudio(this@MainActivity,a.file){showAssets()}}})};setContentView(ScrollView(this).apply{addView(box)})}
 private fun showBuild(){
 val coinFiles=workingFiles().filter{it.name.endsWith(".coin.json")}
 val coins=coinFiles.mapNotNull{f->CoinInspector.inspect(f)?.let{f to it}}
 val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
 box.addView(Button(this).apply{text="← BACK";setOnClickListener{showMain()}})
 box.addView(TextView(this).apply{text="COIN WORKSPACE ("+coins.size+")";textSize=20f;setTextColor(Color.rgb(0,190,255));setPadding(8,12,8,12)})
 coins.take(300).forEach{pair->
  val file=pair.first;val x=pair.second
  box.addView(Button(this).apply{text=x.coinId+"  •  "+x.name+"\n"+x.category+" / "+x.domain+" / "+x.role;setTextColor(Color.WHITE);setBackgroundColor(Color.rgb(5,18,29));setOnClickListener{showCoin(file,x)}})
 }
 setContentView(ScrollView(this).apply{addView(box)})
}
private fun showCoin(file:File,x:org.stratumnova.buildbycoin.coin.CoinSummary){
 val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
 box.addView(Button(this).apply{text="← COINS";setOnClickListener{showBuild()}})
 fun row(k:String,v:String)=TextView(this).apply{text=k+"\n"+if(v.isBlank())"UNRESOLVED" else v;textSize=15f;setTextColor(Color.WHITE);setPadding(12,10,12,10);setBackgroundColor(Color.rgb(5,18,29))}
 box.addView(TextView(this).apply{text=x.coinId;textSize=22f;setTextColor(Color.rgb(0,190,255));setPadding(8,12,8,4)})
 box.addView(TextView(this).apply{text=x.name;textSize=17f;setTextColor(Color.WHITE);setPadding(8,4,8,12)})
 box.addView(row("CATEGORY",x.category));box.addView(row("DOMAIN",x.domain));box.addView(row("ROLE",x.role))
 box.addView(row("ARTIFACT SHA-256",x.sha256));box.addView(row("ARTIFACT BYTES",x.bytes.toString()))
 box.addView(row("COIN FILE",file.name))
 box.addView(row("PROVIDES",x.provides));box.addView(row("REQUIRES",x.requires))
 setContentView(ScrollView(this).apply{addView(box)})
}
 private fun showExport(){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};box.addView(Button(this).apply{text="← BACK";setOnClickListener{showMain()}});box.addView(Button(this).apply{text="PROJECT ZIP";setOnClickListener{startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/zip";putExtra(Intent.EXTRA_TITLE,"BuildByCoin_Project.zip")},SAVE_ZIP)}});val files=store.listOriginals();files.take(30).forEach{file->box.addView(Button(this).apply{text="FILE: "+file.name;setOnClickListener{pendingExport=file;startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/octet-stream";putExtra(Intent.EXTRA_TITLE,file.name)},SAVE_FILE)}})};setContentView(ScrollView(this).apply{addView(box)})}
 private fun showFiles(){
 val fs=store.listOriginals()+workingFiles()
 val coins=fs.count{it.name.endsWith(".coin.json")}
 val assets=AssetBrowser.index(fs).size
 val code=fs.count{it.extension.lowercase() in setOf("js","mjs","kt","java","gd","html","css")}
 val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
 box.addView(Button(this).apply{text="← BACK";setOnClickListener{showMain()}})
 box.addView(TextView(this).apply{text="ZIP / FILE WORKSPACE";textSize=20f;setTextColor(Color.rgb(0,190,255));setPadding(8,8,8,8)})
 box.addView(TextView(this).apply{text="All ("+fs.size+")   Coins ("+coins+")   Assets ("+assets+")   Code ("+code+")";textSize=14f;setTextColor(Color.WHITE);setPadding(8,8,8,8)})
 fs.take(250).forEach{file->box.addView(TextView(this).apply{text=file.name+"   "+file.length()+" B";textSize=13f;setTextColor(Color.WHITE);setPadding(8,8,8,8);setBackgroundColor(Color.rgb(5,18,29))})}
 setContentView(ScrollView(this).apply{addView(box)})
}
}