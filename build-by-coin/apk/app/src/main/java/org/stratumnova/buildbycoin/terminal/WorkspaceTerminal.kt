package org.stratumnova.buildbycoin.terminal
import java.io.File
import org.stratumnova.buildbycoin.diagnostics.BuildDiagnostics
data class TerminalResult(val text:String,val action:String?=null)
class WorkspaceTerminal(private val workspace:File){
 private val history=mutableListOf<String>()
 fun history():List<String> = history.toList()
 fun execute(raw:String):TerminalResult{
  val command=raw.trim();if(command.isBlank())return TerminalResult("");history+=command
  val p=command.lowercase().split(Regex("\\s+"))
  return when(p.first()){
   "help","?"->TerminalResult("COMMANDS\nhelp | files | assets | scene | run | tools | export\nreport | report ai | errors | warnings\nclear")
   "files"->TerminalResult("Opening Files","files");"assets"->TerminalResult("Opening Assets","assets")
   "scene"->TerminalResult("Scene/World editor is the next workspace checkpoint.","scene")
   "run","play"->TerminalResult("Opening Run","run");"tools"->TerminalResult("Opening Build by Coin tools","tools");"export"->TerminalResult("Opening Export","export")
   "report"->{val dir=BuildDiagnostics.scan(workspace,history);TerminalResult("REPORT CREATED\n"+File(dir,"report.txt").absolutePath+"\nUse 'errors' to inspect issues.","report")}
   "errors","warnings"->{val latest=BuildDiagnostics.latest(workspace);if(latest==null)TerminalResult("No report yet. Run: report") else TerminalResult(File(latest,"report.txt").readText())}
   "clear"->TerminalResult("","clear")
   else->TerminalResult("Unknown command: "+command+"\nType 'help'.")
  }
 }
}