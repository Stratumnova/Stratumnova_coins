package org.stratumnova.buildbycoin
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.widget.*
import org.stratumnova.buildbycoin.intake.IntakeScanner
import org.stratumnova.buildbycoin.workspace.WorkspaceStore
import org.stratumnova.buildbycoin.coin.CoinInspector
import org.stratumnova.buildbycoin.coin.CoinEditor
import org.stratumnova.buildbycoin.coin.SidewaysRecord
import org.stratumnova.buildbycoin.coin.SidewaysType
import org.stratumnova.buildbycoin.runtime.GameView
import org.stratumnova.buildbycoin.test.TestPanel
import org.stratumnova.buildbycoin.export.Exporter
import org.stratumnova.buildbycoin.assets.AssetBrowser
import org.stratumnova.buildbycoin.assets.AssetPreview
import org.stratumnova.buildbycoin.terminal.WorkspaceTerminal
import org.stratumnova.buildbycoin.world.WorldStore
import org.stratumnova.buildbycoin.world.WorldEditor
import org.stratumnova.buildbycoin.world.WorldRunView
import org.stratumnova.buildbycoin.world.MapImportSetup
import java.io.File
class MainActivity:Activity(){
 private val UI_BG=Color.rgb(2,7,13);private val UI_PANEL=Color.rgb(5,18,29);private val UI_CYAN=Color.rgb(0,213,255);private val UI_MUTED=Color.rgb(150,180,195)
 private fun uiBg(stroke:Boolean=true)=GradientDrawable().apply{setColor(UI_PANEL);cornerRadius=14f;if(stroke)setStroke(2,UI_CYAN)}
 private fun uiButton(textValue:String)=Button(this).apply{text=textValue;textSize=13f;setTextColor(Color.WHITE);background=uiBg();setPadding(10,4,10,4);isAllCaps=false}
 private fun uiTitle(textValue:String)=TextView(this).apply{text=textValue;textSize=21f;setTextColor(UI_CYAN);setTypeface(typeface,Typeface.BOLD);setPadding(8,12,8,8)}
 private fun uiMeta(textValue:String)=TextView(this).apply{text=textValue;textSize=13f;setTextColor(UI_MUTED);setPadding(8,4,8,10)}
 private fun uiCard(title:String,detail:String)=Button(this).apply{text=title+"\\n"+detail;textSize=13f;gravity=android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL;setTextColor(Color.WHITE);background=uiBg();setPadding(16,10,16,10);isAllCaps=false}

 private lateinit var terminal:TextView;private lateinit var commandLine:EditText;private lateinit var workspaceTerminal:WorkspaceTerminal;private lateinit var worldStore:WorldStore;private lateinit var store:WorkspaceStore;private var mainView:android.view.View?=null;private var lastCartridge:org.stratumnova.buildbycoin.workspace.CartridgeResult?=null
 private val OPEN_FILE=1001;private val OPEN_ZIP=1002;private val OPEN_ASSET=1003;private val OPEN_VERSION=1004;private val OPEN_MAP=1005;private val SAVE_ZIP=2001;private val SAVE_FILE=2002;private val SAVE_TEST=2003;private val SAVE_COIN=2004;private val SAVE_TEMPLATE=2005;private var pendingExport:File?=null;private var pendingExportRoot:File?=null;private var pendingVersionCoinId:String?=null;private var pendingVersionContainer:File?=null;private var pendingVersionId:String="UNRESOLVED";private var pendingParentVersion:String="UNRESOLVED"
 override fun onCreate(state:Bundle?){super.onCreate(state);store=WorkspaceStore(this);workspaceTerminal=WorkspaceTerminal(File(filesDir,"workspace"));worldStore=WorldStore(File(filesDir,"workspace"));val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
 val cyan=Color.rgb(0,190,255);val panel=Color.rgb(5,18,29)
 fun panelBg(stroke:Boolean=true)=GradientDrawable().apply{setColor(panel);cornerRadius=12f;if(stroke)setStroke(2,cyan)}
 fun label(t:String,s:Float=16f)=TextView(this).apply{text=t;textSize=s;setTextColor(Color.WHITE);setPadding(8,8,8,8)}
 fun tab(n:String)=Button(this).apply{text=n;textSize=12f;setTextColor(Color.WHITE);background=panelBg();setPadding(4,2,4,2)}
 root.addView(label("◈  STRATUMNOVA WORKSPACE",24f).apply{setTextColor(cyan);setTypeface(typeface,Typeface.BOLD)});root.addView(label("MOBILE GAME + WORLD BUILDER  •  ANDROID / WEB",12f).apply{setTextColor(cyan)});val tabs=LinearLayout(this)
 listOf("Terminal","Scene","Files","Assets","Run","Tools").forEach { n ->
  val button=tab(n).apply { setOnClickListener {
   when(n){"Terminal"->showMain();"Scene"->showScene();"Files"->showFiles();"Assets"->showAssets();"Run"->openRun();"Tools"->showBuild()}
  }}
  tabs.addView(button,LinearLayout.LayoutParams(0,56,1f))
 }
 root.addView(tabs)
 terminal=label("> STRATUMNOVA WORKSPACE\n> Terminal is the shared project bus.\n> Type help for commands.\n> Build reports: report | errors\n> Runtime: v0.1.0",15f).apply{setTextColor(Color.rgb(0,255,150));typeface=Typeface.MONOSPACE;background=panelBg()};root.addView(terminal,LinearLayout.LayoutParams(-1,0,1f));val commandRow=LinearLayout(this);commandLine=EditText(this).apply{hint="> command";setTextColor(Color.WHITE);setHintTextColor(Color.GRAY);setSingleLine(true);typeface=Typeface.MONOSPACE};commandRow.addView(commandLine,LinearLayout.LayoutParams(0,-2,1f));commandRow.addView(uiButton("RUN").apply{setOnClickListener{runTerminalCommand()}},LinearLayout.LayoutParams(-2,-2));root.addView(commandRow);commandLine.setOnEditorActionListener{_,_,_->runTerminalCommand();true};root.addView(label("INTAKE",18f));val intake=LinearLayout(this)
 fun b(n:String,c:Int,t:String):Button { return Button(this).apply { text=n; setOnClickListener { pick(c,t) } } }
 intake.addView(b("FILE",OPEN_FILE,"*/*"),LinearLayout.LayoutParams(0,-2,1f))
 intake.addView(b("ZIP",OPEN_ZIP,"application/zip"),LinearLayout.LayoutParams(0,-2,1f))
 intake.addView(b("ASSET",OPEN_ASSET,"*/*"),LinearLayout.LayoutParams(0,-2,1f))
 intake.addView(b("MAP",OPEN_MAP,"image/*"),LinearLayout.LayoutParams(0,-2,1f))
 root.addView(intake)
 root.addView(label("Single files for quick intake; ZIP for cartridges and large sets.",14f))
 mainView=root;setContentView(root)
 }
 private fun runTerminalCommand(){
  val raw=commandLine.text.toString();if(raw.isBlank())return
  val result=workspaceTerminal.execute(raw);commandLine.setText("")
  terminal.text=if(result.text.isBlank())">" else "> "+raw+"\n"+result.text
  when(result.action){"files"->showFiles();"assets"->showAssets();"scene"->showScene();"run"->openRun();"tools"->showBuild();"export"->showExport();"clear"->terminal.text=""}
 }
 private fun showScene(){
  WorldEditor.show(this,worldStore,AssetBrowser.index(workingFiles()).map{it.file},{showMain()},{showMain();commandLine.requestFocus()})
 }
 private fun pick(c:Int,t:String)=startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type=t},c)
 override fun onActivityResult(r:Int,rc:Int,data:Intent?){super.onActivityResult(r,rc,data);if(rc!=RESULT_OK)return;val uri=data?.data?:return;try{if(r==OPEN_MAP){val bitmap=contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it)}?:throw IllegalStateException("Map image could not be decoded");MapImportSetup.show(this,bitmap,worldStore,File(filesDir,"workspace")){showScene()};return};if(r==SAVE_ZIP){Exporter.projectZip(this,File(filesDir,"workspace"),uri);terminal.text="> PROJECT ZIP EXPORTED";return};if(r==SAVE_TEST){pendingExportRoot?.let{Exporter.projectZip(this,it,uri)};terminal.text="> TEST PACKAGE EXPORTED";return};if(r==SAVE_COIN){pendingExportRoot?.let{Exporter.projectZip(this,it,uri)};terminal.text="> COIN CONTAINER EXPORTED";return};if(r==SAVE_TEMPLATE){pendingExportRoot?.let{Exporter.projectZip(this,it,uri)};terminal.text="> TEMPLATE EXPORTED";return};if(r==SAVE_FILE){pendingExport?.let{Exporter.singleFile(this,it,uri)};terminal.text="> SINGLE FILE EXPORTED";return};if(r==OPEN_VERSION){
 val coinId=pendingVersionCoinId?:return
 val container=pendingVersionContainer?:return
 val scanner=IntakeScanner(this);val entry=scanner.scanSingle(uri)
 val versions=File(container,"versions");versions.mkdirs()
 val target=uniqueVersionFile(versions,entry.name)
 contentResolver.openInputStream(uri)!!.use{i->target.outputStream().use{o->i.copyTo(o)}}
 if(target.length()!=entry.bytes){target.delete();throw IllegalStateException("version byte verification failed")}
 val record=SidewaysRecord.create(coinId,target,SidewaysType.VERSION,"Imported revised artifact; baseline preserved",pendingVersionId,pendingParentVersion)
 val side=File(container,"sideways");side.mkdirs()
 File(side,coinId+"_version_"+System.currentTimeMillis()+".sideways.json").writeText(record.toString(2))
 terminal.text="> VERSION PRESERVED\n> "+coinId+"\n> "+target.name+"\n> SHA-256: "+entry.sha256+"\n> Baseline unchanged."
 pendingVersionCoinId=null;pendingVersionContainer=null;pendingVersionId="UNRESOLVED";pendingParentVersion="UNRESOLVED";showMain();return
};val scanner=IntakeScanner(this);if(r==OPEN_ZIP){val items=scanner.scanZip(uri);val groups=items.groupingBy{it.kind}.eachCount();val cartridge=store.importZip(uri);lastCartridge=cartridge;terminal.text="> ZIP scanned\n> Entries: "+items.size+"\n"+groups.entries.sortedByDescending{it.value}.joinToString("\n"){"> "+it.key+": "+it.value}+"\n> Cartridge: "+cartridge.root.name+"\n> Extracted files: "+cartridge.files.size+"\n> Source ZIP not modified."}else{val e=scanner.scanSingle(uri);val saved=store.preserveSingle(uri,e);val c=CoinInspector.inspect(saved.file);terminal.text=if(c!=null)"> COIN PRESERVED\n> "+c.coinId+"\n> "+c.name+"\n> "+c.category+" / "+c.domain+"\n> Role: "+c.role+"\n> READ ONLY\n> SHA-256: "+e.sha256 else "> FILE PRESERVED\n> "+e.name+"\n> Type: "+e.kind+"\n> Bytes: "+e.bytes+"\n> SHA-256: "+e.sha256+"\n> Candidate coin fields: UNRESOLVED"}}catch(e:Exception){terminal.text="> INTAKE ERROR\n> "+e.javaClass.simpleName+": "+e.message}}
 private fun showMain(){mainView?.let{setContentView(it)}}
 override fun onBackPressed(){if(mainView!=null && mainView?.parent==null)showMain() else super.onBackPressed()}
 private fun workingFiles():List<File>{val w=File(filesDir,"workspace/working");return if(w.exists())w.walkTopDown().filter{it.isFile}.toList() else emptyList()}
 private fun showAssets(){
  val assets=AssetBrowser.index(workingFiles())
  val images=assets.count{it.kind=="IMAGE"};val audio=assets.count{it.kind=="AUDIO"}
  val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
  box.addView(uiButton("←  Back").apply{setOnClickListener{showMain()}})
  box.addView(uiTitle("ASSET WORKSHOP"))
  box.addView(uiMeta("ALL  "+assets.size+"    IMAGES  "+images+"    AUDIO  "+audio))
  if(assets.isEmpty())box.addView(TextView(this).apply{text="No image or audio assets in the active workspace.";setTextColor(Color.WHITE);setPadding(8,16,8,16)})
  assets.take(250).forEach{a->
   box.addView(Button(this).apply{
    text=(if(a.kind=="IMAGE")"▣  IMAGE" else "♪  AUDIO")+"\n"+a.file.name+"   "+a.file.length()+" B"
    setTextColor(Color.WHITE);setBackgroundColor(Color.rgb(5,18,29))
    setOnClickListener{if(a.kind=="IMAGE")AssetPreview.showImage(this@MainActivity,a.file){showAssets()} else AssetPreview.showAudio(this@MainActivity,a.file){showAssets()}}
   })
  }
  setContentView(ScrollView(this).apply{addView(box)})
 }
 private fun showBuild(){
 val coinFiles=workingFiles().filter{it.name.endsWith(".coin.json")}
 val coins=coinFiles.mapNotNull{f->CoinInspector.inspect(f)?.let{f to it}}
 val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
 box.addView(Button(this).apply{text="← BACK";setOnClickListener{showMain()}})
 box.addView(uiTitle("COIN WORKSPACE  "+coins.size))
 coins.take(300).forEach{pair->
  val file=pair.first;val x=pair.second
  box.addView(Button(this).apply{text=x.coinId+"  •  "+x.name+"\n"+x.category+" / "+x.domain+" / "+x.role;setTextColor(Color.WHITE);setBackgroundColor(Color.rgb(5,18,29));setOnClickListener{showCoin(file,x)}})
 }
 setContentView(ScrollView(this).apply{addView(box)})
}
private fun showCoin(file:File,x:org.stratumnova.buildbycoin.coin.CoinSummary){
 val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
 box.addView(uiButton("←  Coins").apply{setOnClickListener{showBuild()}})
 fun row(k:String,v:String)=TextView(this).apply{text=k+"\n"+if(v.isBlank())"UNRESOLVED" else v;textSize=15f;setTextColor(Color.WHITE);setPadding(12,10,12,10);setBackgroundColor(Color.rgb(5,18,29))}
 box.addView(TextView(this).apply{text=x.coinId;textSize=22f;setTextColor(Color.rgb(0,190,255));setPadding(8,12,8,4)})
 box.addView(TextView(this).apply{text=x.name;textSize=17f;setTextColor(Color.WHITE);setPadding(8,4,8,12)})
 box.addView(row("CATEGORY",x.category));box.addView(row("DOMAIN",x.domain));box.addView(row("ROLE",x.role))
 box.addView(row("ARTIFACT SHA-256",x.sha256));box.addView(row("ARTIFACT BYTES",x.bytes.toString()))
 box.addView(row("COIN FILE",file.name))
 box.addView(row("PROVIDES",x.provides));box.addView(row("REQUIRES",x.requires))
 val actions=LinearLayout(this)
 actions.addView(Button(this).apply{text="ADD VERSION";setOnClickListener{pickVersion(x.coinId,file)}},LinearLayout.LayoutParams(0,-2,1f))
 actions.addView(Button(this).apply{text="ADD EVIDENCE";setOnClickListener{attachEvidence(x.coinId,file)}},LinearLayout.LayoutParams(0,-2,1f))
 box.addView(actions)
 val side=File(file.parentFile,"sideways")
 val records=side.listFiles()?.filter{it.isFile&&it.name.endsWith(".sideways.json")}?.sortedBy{it.name}.orEmpty()
 box.addView(row("SIDEWAYS RECORDS",records.size.toString()))
 val activeFile=File(file.parentFile,"active-version.json")
 val active=if(activeFile.isFile)try{org.json.JSONObject(activeFile.readText()).optString("version_id","BASELINE")}catch(_:Exception){"BASELINE"}else "BASELINE"
 box.addView(row("ACTIVE VERSION",active))
 val versionChoices=mutableListOf("BASELINE")
 records.forEach{r->try{val j=org.json.JSONObject(r.readText());if(j.optString("sideways_type")=="VERSION"){val v=j.optString("version_id","UNRESOLVED");if(v!="UNRESOLVED"&&!versionChoices.contains(v))versionChoices+=v}}catch(_:Exception){}}
 box.addView(Button(this).apply{text="SELECT ACTIVE VERSION";setOnClickListener{
  AlertDialog.Builder(this@MainActivity).setTitle("Active version → "+x.coinId).setItems(versionChoices.toTypedArray()){_,which->
   val chosen=versionChoices[which]
   activeFile.writeText(org.json.JSONObject().put("schema","a25.active-version.v0.1").put("master_coin_id",x.coinId).put("version_id",chosen).toString(2))
   Toast.makeText(this@MainActivity,"Active version: "+chosen,Toast.LENGTH_SHORT).show();showCoin(file,x)
  }.show()
 }})
 records.takeLast(40).forEach{r->
  try{
   val j=org.json.JSONObject(r.readText())
   val kind=j.optString("sideways_type","UNRESOLVED")
   val version=j.optString("version_id","UNRESOLVED")
   val parent=j.optString("parent_version","UNRESOLVED")
   val artifact=j.optString("name","UNRESOLVED")
   val sha=j.optString("sha256","UNRESOLVED")
   box.addView(TextView(this).apply{
    text=kind+"  "+version+"\nParent: "+parent+"\n"+artifact+"\nSHA-256: "+sha
    textSize=12f;setTextColor(Color.WHITE);setPadding(12,10,12,10);setBackgroundColor(Color.rgb(5,18,29))
   })
  }catch(_:Exception){
   box.addView(row("SIDEWAYS RECORD",r.name+" — unreadable"))
  }
 }
 setContentView(ScrollView(this).apply{addView(box)})
}
 private fun pickVersion(masterCoinId:String,coinFile:File){
  val container=coinFile.parentFile?:return
  val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,8,24,8)}
  val version=EditText(this).apply{hint="Version ID, e.g. v1.1"}
  val parent=EditText(this).apply{hint="Parent version, e.g. v1"}
  box.addView(version);box.addView(parent)
  AlertDialog.Builder(this).setTitle("Version lineage → "+masterCoinId).setView(box)
   .setNegativeButton("CANCEL",null).setPositiveButton("CHOOSE ARTIFACT"){_,_->
    pendingVersionCoinId=masterCoinId;pendingVersionContainer=container
    pendingVersionId=version.text.toString().trim().ifBlank{"UNRESOLVED"}
    pendingParentVersion=parent.text.toString().trim().ifBlank{"UNRESOLVED"}
    startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="*/*"},OPEN_VERSION)
   }.show()
 }
 private fun uniqueVersionFile(dir:File,name:String):File{
  val clean=name.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"),"_").ifBlank{"version_artifact"}
  var out=File(dir,clean);if(!out.exists())return out
  var n=1
  while(true){val suffix=if(out.extension.isBlank())"" else "."+out.extension;val candidate=File(dir,out.nameWithoutExtension+"_"+n+suffix);if(!candidate.exists())return candidate;n++}
 }
 private fun attachEvidence(masterCoinId:String,coinFile:File){
  val evidenceRoot=File(filesDir,"workspace/evidence")
  val runs=evidenceRoot.listFiles()?.filter{it.isDirectory&&File(it,"report.json").isFile}?.sortedByDescending{it.lastModified()}.orEmpty()
  if(runs.isEmpty()){Toast.makeText(this,"No test evidence available yet",Toast.LENGTH_SHORT).show();return}
  val names=runs.map{it.name}.toTypedArray()
  AlertDialog.Builder(this).setTitle("Attach test evidence → "+masterCoinId).setItems(names){_,which->
   val run=runs[which];val report=File(run,"report.json")
   val container=coinFile.parentFile?:return@setItems
   val dest=File(container,"evidence/"+run.name);dest.mkdirs()
   run.listFiles()?.filter{it.isFile}?.forEach{src->src.copyTo(File(dest,src.name),overwrite=false)}
   val record=SidewaysRecord.create(masterCoinId,report,SidewaysType.EVIDENCE,"Attached test evidence "+run.name)
   val side=File(container,"sideways");side.mkdirs()
   File(side,masterCoinId+"_evidence_"+System.currentTimeMillis()+".sideways.json").writeText(record.toString(2))
   Toast.makeText(this,"Evidence attached: "+run.name,Toast.LENGTH_SHORT).show()
  }.show()
 }
 private fun createSideways(masterCoinId:String,source:File,type:SidewaysType){
  val input=EditText(this).apply{hint="Short note — optional"}
  AlertDialog.Builder(this).setTitle(type.name+" → "+masterCoinId).setView(input).setNegativeButton("CANCEL",null).setPositiveButton("CREATE"){_,_->
   val root=source.parentFile?:File(filesDir,"workspace/working")
   val side=File(root,"sideways");side.mkdirs()
   val record=SidewaysRecord.create(masterCoinId,source,type,input.text.toString())
   val stamp=System.currentTimeMillis()
   val out=File(side,masterCoinId+"_"+type.name.lowercase()+"_"+stamp+".sideways.json")
   out.writeText(record.toString(2))
   Toast.makeText(this,"Sideways "+type.name+" record created",Toast.LENGTH_SHORT).show()
  }.show()
 }
 private fun showExport(){
  val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
  box.addView(Button(this).apply{text="← BACK";setOnClickListener{showMain()}})
  box.addView(uiTitle("EXPORT"))
  box.addView(TextView(this).apply{text="Portable snapshots. Workspace remains unchanged.";setTextColor(Color.WHITE);setPadding(8,4,8,12)})
  fun action(name:String,detail:String,run:()->Unit){box.addView(Button(this).apply{text=name+"\n"+detail;setTextColor(Color.WHITE);setBackgroundColor(Color.rgb(5,18,29));setOnClickListener{run()}})}
  action("PROJECT ZIP","Complete current workspace"){startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/zip";putExtra(Intent.EXTRA_TITLE,"BuildByCoin_Project.zip")},SAVE_ZIP)}
  val evidence=File(filesDir,"workspace/evidence")
  action("TEST PACKAGE","Evidence and reports only"){if(evidence.exists()){pendingExportRoot=evidence;startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/zip";putExtra(Intent.EXTRA_TITLE,"BuildByCoin_TestPackage.zip")},SAVE_TEST)}else Toast.makeText(this,"No test evidence yet",Toast.LENGTH_SHORT).show()}
  action("SINGLE FILE","Choose from preserved originals"){showSingleFileExport()}
  action("COIN CONTAINER","Select a real Coin and export its source container"){showCoinContainerExport()}
  action("TEMPLATE","Portable clean project/runtime scaffold"){
   val template=File(filesDir,"workspace/template")
   if(template.exists())template.deleteRecursively()
   template.mkdirs()
   File(template,"project.json").writeText("""{"schema":"build-by-coin.project.v0.1","name":"Untitled","runtime":"v0.1.0","seed":25,"tick_rate":30,"coins":[],"assets":[],"tests":[]}""")
   File(template,"README.txt").writeText("Build by Coin portable template\nRuntime: v0.1.0\nNo source Coins or assets are embedded until explicitly added.\n")
   File(template,"coins").mkdirs();File(template,"assets").mkdirs();File(template,"tests").mkdirs()
   pendingExportRoot=template
   startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/zip";putExtra(Intent.EXTRA_TITLE,"BuildByCoin_Template_v0.1.zip")},SAVE_TEMPLATE)
  }
  setContentView(ScrollView(this).apply{addView(box)})
 }
 private fun showCoinContainerExport(){
  val coinFiles=workingFiles().filter{it.name.endsWith(".coin.json")}
  val coins=coinFiles.mapNotNull{f->CoinInspector.inspect(f)?.let{f to it}}
  val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
  box.addView(uiButton("←  Export").apply{setOnClickListener{showExport()}})
  box.addView(TextView(this).apply{text="COIN CONTAINER ("+coins.size+")";textSize=20f;setTextColor(Color.rgb(0,190,255));setPadding(8,12,8,12)})
  coins.take(300).forEach{pair->
   val coinFile=pair.first;val x=pair.second
   box.addView(Button(this).apply{text=x.coinId+"  •  "+x.name;setOnClickListener{
    val container=coinFile.parentFile
    if(container==null){Toast.makeText(this@MainActivity,"Coin container unavailable",Toast.LENGTH_SHORT).show()}else{
     pendingExportRoot=container
     startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/zip";putExtra(Intent.EXTRA_TITLE,x.coinId+"_container.zip")},SAVE_COIN)
    }
   }})
  }
  setContentView(ScrollView(this).apply{addView(box)})
 }
 private fun showSingleFileExport(){
  val files=store.listOriginals()+workingFiles()
  val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
  box.addView(Button(this).apply{text="← EXPORT";setOnClickListener{showExport()}})
  box.addView(TextView(this).apply{text="SINGLE FILE ("+files.size+")";textSize=20f;setTextColor(Color.rgb(0,190,255));setPadding(8,12,8,12)})
  files.take(250).forEach{file->box.addView(Button(this).apply{text=file.name+"   "+file.length()+" B";setOnClickListener{pendingExport=file;startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/octet-stream";putExtra(Intent.EXTRA_TITLE,file.name)},SAVE_FILE)}})}
  setContentView(ScrollView(this).apply{addView(box)})
 }
 private fun openRun(){
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(10,10,10,10);setBackgroundColor(UI_BG)}
  val nav=LinearLayout(this)
  nav.addView(uiButton("← Back").apply{setOnClickListener{showMain()}},LinearLayout.LayoutParams(0,-2,1f))
  nav.addView(uiButton("BUILD").apply{setOnClickListener{showScene()}},LinearLayout.LayoutParams(0,-2,1f))
  nav.addView(uiButton(">_ Terminal").apply{setOnClickListener{showMain();commandLine.requestFocus()}},LinearLayout.LayoutParams(0,-2,1f));root.addView(nav)
  val status=uiMeta("LIVE WORLD • swipe/tap toward movement direction");root.addView(status)
  val game=WorldRunView(this,worldStore);game.onState={status.text="LIVE WORLD • "+it};root.addView(game,LinearLayout.LayoutParams(-1,0,1f))
  setContentView(root)
 }
 private fun showFiles(){
 val fs=store.listOriginals()+workingFiles()
 val coins=fs.count{it.name.endsWith(".coin.json")}
 val assets=AssetBrowser.index(fs).size
 val code=fs.count{it.extension.lowercase() in setOf("js","mjs","kt","java","gd","html","css")}
 val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16);setBackgroundColor(Color.rgb(2,7,13))}
 box.addView(Button(this).apply{text="← BACK";setOnClickListener{showMain()}})
 box.addView(uiTitle("ZIP / FILE WORKSPACE"))
 box.addView(uiMeta("ALL  "+fs.size+"    COINS  "+coins+"    ASSETS  "+assets+"    CODE  "+code))
 fs.take(250).forEach{file->
  val isCoin=file.name.endsWith(".coin.json")
  box.addView(Button(this).apply{
   text=(if(isCoin)"COIN  " else "FILE  ")+file.name+"   "+file.length()+" B"
   setTextColor(Color.WHITE);setBackgroundColor(Color.rgb(5,18,29))
   setOnClickListener{
    if(isCoin){CoinInspector.inspect(file)?.let{showCoin(file,it)}}
    else CoinEditor.candidateDialog(this@MainActivity,file){json->
     val dir=File(filesDir,"workspace/working/candidates");dir.mkdirs()
     val out=File(dir,file.name+".candidate.coin.json");out.writeText(json)
     Toast.makeText(this@MainActivity,"Candidate saved: "+out.name,Toast.LENGTH_SHORT).show()
    }
   }
  })
 }
 setContentView(ScrollView(this).apply{addView(box)})
}
}