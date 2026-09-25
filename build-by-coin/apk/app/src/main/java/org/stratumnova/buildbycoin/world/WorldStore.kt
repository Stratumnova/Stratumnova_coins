package org.stratumnova.buildbycoin.world

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class SpaceKind { FLOOR, WALL, PATH, WATER, OBJECT, SPAWN, EXIT }

data class CellSpace(val x:Int,val y:Int,var kind:SpaceKind=SpaceKind.FLOOR,var label:String="",var assetPath:String="",var targetCell:String=""){
 val code:String get()="S-X"+x.toString().padStart(2,'0')+"-Y"+y.toString().padStart(2,'0')
}

data class WorldCell(val id:String,val name:String,var worldX:Int,var worldY:Int,val spaces:MutableList<CellSpace>)

class WorldStore(private val root:File){
 private val worldDir=File(root,"world").apply{mkdirs()}
 private val cellsDir=File(worldDir,"cells").apply{mkdirs()}
 private val manifest=File(worldDir,"world.json")
 init{if(!manifest.exists())createDefault()}
 private fun createDefault(){
  val cell=WorldCell("CELL-001","Cell 1",0,0,MutableList(81){i->CellSpace(i%9,i/9)})
  saveCell(cell)
  saveWorld(JSONObject().put("schema","stratumnova.world.v0.1").put("name","Untitled World").put("active_cell","CELL-001").put("cells",JSONArray().put(JSONObject().put("id","CELL-001").put("name","Cell 1").put("x",0).put("y",0))))
 }
 fun world():JSONObject=try{JSONObject(manifest.readText())}catch(_:Exception){JSONObject()}
 private fun saveWorld(j:JSONObject)=manifest.writeText(j.toString(2))
 fun cells():List<JSONObject>{val a=world().optJSONArray("cells")?:JSONArray();return (0 until a.length()).mapNotNull{a.optJSONObject(it)}}
 fun activeId():String=world().optString("active_cell","CELL-001")
 fun setActive(id:String){val j=world();j.put("active_cell",id);saveWorld(j)}
 fun loadCell(id:String):WorldCell{
  val f=File(cellsDir,id+".json");if(!f.exists())return WorldCell(id,id,0,0,MutableList(81){i->CellSpace(i%9,i/9)})
  val j=JSONObject(f.readText());val a=j.optJSONArray("spaces")?:JSONArray()
  val spaces=MutableList(81){i->CellSpace(i%9,i/9)}
  for(i in 0 until a.length()){val s=a.optJSONObject(i)?:continue;val x=s.optInt("x");val y=s.optInt("y");val index=y*9+x;if(index in spaces.indices)spaces[index]=CellSpace(x,y,runCatching{SpaceKind.valueOf(s.optString("kind","FLOOR"))}.getOrDefault(SpaceKind.FLOOR),s.optString("label",""),s.optString("asset_path",""),s.optString("target_cell",""))}
  return WorldCell(id,j.optString("name",id),j.optInt("world_x"),j.optInt("world_y"),spaces)
 }
 fun saveCell(c:WorldCell){
  val a=JSONArray();c.spaces.forEach{s->a.put(JSONObject().put("code",s.code).put("x",s.x).put("y",s.y).put("kind",s.kind.name).put("label",s.label).put("asset_path",s.assetPath).put("target_cell",s.targetCell))}
  File(cellsDir,c.id+".json").writeText(JSONObject().put("schema","stratumnova.cell.v0.1").put("id",c.id).put("name",c.name).put("world_x",c.worldX).put("world_y",c.worldY).put("width",9).put("height",9).put("spaces",a).toString(2))
 }

 fun moveCell(id:String,x:Int,y:Int):Boolean{
  val j=world();val a=j.optJSONArray("cells")?:return false
  for(i in 0 until a.length()){val item=a.optJSONObject(i)?:continue;if(item.optString("id")==id){item.put("x",x).put("y",y);val cell=loadCell(id);cell.worldX=x;cell.worldY=y;saveCell(cell);saveWorld(j);return true}}
  return false
 }
 fun cellAt(x:Int,y:Int):String?=cells().firstOrNull{it.optInt("x")==x&&it.optInt("y")==y}?.optString("id")
 fun connectAdjacent(){
  val all=cells()
  all.forEach{meta->
   val id=meta.optString("id");val cx=meta.optInt("x");val cy=meta.optInt("y");val cell=loadCell(id)
   val links=listOf(Triple(8,4,cellAt(cx+1,cy)),Triple(0,4,cellAt(cx-1,cy)),Triple(4,0,cellAt(cx,cy-1)),Triple(4,8,cellAt(cx,cy+1)))
   links.forEach{(x,y,target)->val s=cell.spaces[y*9+x];if(target!=null){s.kind=SpaceKind.EXIT;s.targetCell=target;if(s.label.isBlank())s.label="EXIT"}else if(s.label=="EXIT"&&s.targetCell.isNotBlank()){s.kind=SpaceKind.FLOOR;s.targetCell="";s.label=""}}
   saveCell(cell)
  }
 }
 fun replaceFromDraft(cellsX:Int,cellsY:Int,kindAt:(Int,Int,Int,Int)->SpaceKind){
  cellsDir.listFiles()?.filter{it.isFile&&it.extension=="json"}?.forEach{it.delete()}
  val arr=JSONArray();var n=1
  for(wy in 0 until cellsY)for(wx in 0 until cellsX){val id="CELL-"+n.toString().padStart(3,'0');val spaces=MutableList(81){i->CellSpace(i%9,i/9,kindAt(wx,wy,i%9,i/9))};val cell=WorldCell(id,"Cell "+n,wx,wy,spaces);saveCell(cell);arr.put(JSONObject().put("id",id).put("name",cell.name).put("x",wx).put("y",wy));n++}
  saveWorld(JSONObject().put("schema","stratumnova.world.v0.1").put("name","Imported World").put("active_cell","CELL-001").put("cells",arr));connectAdjacent()
 }
 fun addCell():WorldCell{
  val j=world();val a=j.optJSONArray("cells")?:JSONArray();val n=a.length()+1;val id="CELL-"+n.toString().padStart(3,'0')
  val cell=WorldCell(id,"Cell "+n,n-1,0,MutableList(81){i->CellSpace(i%9,i/9)});saveCell(cell)
  a.put(JSONObject().put("id",id).put("name",cell.name).put("x",cell.worldX).put("y",cell.worldY));j.put("cells",a).put("active_cell",id);saveWorld(j);return cell
 }
}