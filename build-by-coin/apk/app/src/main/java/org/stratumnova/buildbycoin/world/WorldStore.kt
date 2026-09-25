package org.stratumnova.buildbycoin.world

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class SpaceKind { FLOOR, WALL, PATH, WATER, OBJECT, SPAWN, EXIT }

data class CellSpace(val x:Int,val y:Int,var kind:SpaceKind=SpaceKind.FLOOR,var label:String=""){
 val code:String get()="S-X"+x.toString().padStart(2,'0')+"-Y"+y.toString().padStart(2,'0')
}

data class WorldCell(val id:String,val name:String,val worldX:Int,val worldY:Int,val spaces:MutableList<CellSpace>)

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
  for(i in 0 until a.length()){val s=a.optJSONObject(i)?:continue;val x=s.optInt("x");val y=s.optInt("y");val index=y*9+x;if(index in spaces.indices)spaces[index]=CellSpace(x,y,runCatching{SpaceKind.valueOf(s.optString("kind","FLOOR"))}.getOrDefault(SpaceKind.FLOOR),s.optString("label",""))}
  return WorldCell(id,j.optString("name",id),j.optInt("world_x"),j.optInt("world_y"),spaces)
 }
 fun saveCell(c:WorldCell){
  val a=JSONArray();c.spaces.forEach{s->a.put(JSONObject().put("code",s.code).put("x",s.x).put("y",s.y).put("kind",s.kind.name).put("label",s.label))}
  File(cellsDir,c.id+".json").writeText(JSONObject().put("schema","stratumnova.cell.v0.1").put("id",c.id).put("name",c.name).put("world_x",c.worldX).put("world_y",c.worldY).put("width",9).put("height",9).put("spaces",a).toString(2))
 }
 fun addCell():WorldCell{
  val j=world();val a=j.optJSONArray("cells")?:JSONArray();val n=a.length()+1;val id="CELL-"+n.toString().padStart(3,'0')
  val cell=WorldCell(id,"Cell "+n,n-1,0,MutableList(81){i->CellSpace(i%9,i/9)});saveCell(cell)
  a.put(JSONObject().put("id",id).put("name",cell.name).put("x",cell.worldX).put("y",cell.worldY));j.put("cells",a).put("active_cell",id);saveWorld(j);return cell
 }
}