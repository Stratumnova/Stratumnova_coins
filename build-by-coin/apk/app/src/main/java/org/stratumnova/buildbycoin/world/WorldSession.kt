package org.stratumnova.buildbycoin.world
class WorldSession(private val store:WorldStore){
 var cellId=store.activeId();private set
 var x=4;private set;var y=4;private set
 init{spawn()}
 private fun cell()=store.loadCell(cellId)
 private fun spawn(preferX:Int?=null,preferY:Int?=null){val c=cell();val s=c.spaces.firstOrNull{it.kind==SpaceKind.SPAWN};x=s?.x?:preferX?:4;y=s?.y?:preferY?:4}
 private fun walkable(s:CellSpace)=s.kind!=SpaceKind.WALL&&s.kind!=SpaceKind.WATER
 fun move(dx:Int,dy:Int):Boolean{
  val c=cell();val nx=x+dx;val ny=y+dy
  if(nx !in 0..8||ny !in 0..8)return false
  val s=c.spaces[ny*9+nx];if(!walkable(s))return false;x=nx;y=ny
  if(s.kind==SpaceKind.EXIT&&s.targetCell.isNotBlank()){val oldX=x;val oldY=y;cellId=s.targetCell;store.setActive(cellId);spawn(when{oldX==8->0;oldX==0->8;else->oldX},when{oldY==8->0;oldY==0->8;else->oldY})}
  return true
 }
}