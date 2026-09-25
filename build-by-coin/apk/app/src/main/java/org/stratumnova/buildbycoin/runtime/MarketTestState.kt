package org.stratumnova.buildbycoin.runtime

import java.util.Random
import kotlin.math.max
import kotlin.math.roundToInt

enum class CraftRandomMode { SEEDED, RANDOM }
enum class MarketKind { FOOD, ITEM, HYBRID }

data class MarketIngredient(
    val id:String,
    val name:String,
    val food:Int,
    val hardness:Int,
    val sharpness:Int,
    val nutrition:Int,
    val durability:Int,
    val conductivity:Int,
    val freshness:Int,
    val quality:Int,
    val rarity:Int,
    val baseValue:Int
)

data class GeneratedMarketItem(
    val id:String,
    val name:String,
    val kind:MarketKind,
    val food:Int,
    val hardness:Int,
    val sharpness:Int,
    val nutrition:Int,
    val durability:Int,
    val conductivity:Int,
    val freshness:Int,
    val quality:Int,
    val rarity:Int,
    val marketValue:Int,
    val marketBuysFor:Int,
    val marketSellsFor:Int,
    val sourceIds:List<String>,
    val mode:CraftRandomMode,
    val seed:Long?
)

data class Recipe(val item:String,val parts:List<String>,val saleValue:Int)
data class StallState(val id:String,val item:String,var stock:Int=0)

class MarketTestState{
    var marketCredits=100
    var playerCredits=100
    val customerCredits=linkedMapOf("CUSTOMER-1" to 150,"CUSTOMER-2" to 150,"CUSTOMER-3" to 150)
    val recipes=linkedMapOf(
        "ITEM-1" to Recipe("ITEM-1",listOf("PART-A","PART-B"),10),
        "ITEM-2" to Recipe("ITEM-2",listOf("PART-C","PART-D"),12),
        "ITEM-3" to Recipe("ITEM-3",listOf("PART-E","PART-F"),15)
    )
    val bins=linkedMapOf("PART-A" to 3,"PART-B" to 3,"PART-C" to 3,"PART-D" to 3,"PART-E" to 3,"PART-F" to 3)
    val stalls=linkedMapOf(
        "STALL-1" to StallState("STALL-1","ITEM-1"),
        "STALL-2" to StallState("STALL-2","ITEM-2"),
        "STALL-3" to StallState("STALL-3","ITEM-3")
    )

    var craftMode=CraftRandomMode.SEEDED
    var craftSeed=25L
    val craftSlots=arrayOfNulls<String>(3)
    var lastGenerated:GeneratedMarketItem?=null
    val generatedInventory=mutableListOf<GeneratedMarketItem>()

    // Mixed food / ecology / salvage / Scrapline-style feedstock. These are inputs, not a finite output list.
    val stationInputs=linkedMapOf(
        "FISH" to MarketIngredient("FISH","River Fish",90,15,20,80,20,5,85,55,25,18),
        "CORN" to MarketIngredient("CORN","Dry Corn",95,10,5,65,15,2,75,45,15,10),
        "ROOT" to MarketIngredient("ROOT","Ironroot",80,45,15,55,40,8,70,60,35,22),
        "MEAT" to MarketIngredient("MEAT","Game Meat",95,20,25,90,18,3,65,65,30,26),
        "BERRY" to MarketIngredient("BERRY","Nova Berry",100,5,3,45,5,4,90,70,55,30),
        "MUSHROOM" to MarketIngredient("MUSHROOM","Cave Cap",100,8,4,35,8,10,60,55,45,20),
        "SCRAP" to MarketIngredient("SCRAP","Mixed Scrap",0,70,35,0,75,35,0,45,20,24),
        "PLATE" to MarketIngredient("PLATE","Bot Plate",0,90,20,0,95,20,0,70,40,42),
        "BLADE" to MarketIngredient("BLADE","Bot Blade",0,82,95,0,70,15,0,75,55,55),
        "WIRE" to MarketIngredient("WIRE","Copper Wire",0,35,10,0,40,95,0,65,35,34),
        "CELL" to MarketIngredient("CELL","Power Cell",0,45,5,0,60,100,0,80,70,68),
        "TOOTH" to MarketIngredient("TOOTH","Predator Tooth",10,85,88,5,72,2,30,68,60,48)
    )

    fun setCraftSlot(slot:Int,itemId:String?):Boolean{
        if(slot !in 0..2)return false
        if(itemId!=null&&!stationInputs.containsKey(itemId))return false
        craftSlots[slot]=itemId
        return true
    }

    fun clearCraftSlots(){for(i in craftSlots.indices)craftSlots[i]=null}

    fun randomCraft():GeneratedMarketItem?{
        val inputs=craftSlots.mapNotNull{stationInputs[it]}.take(3)
        if(inputs.isEmpty())return null
        val random=if(craftMode==CraftRandomMode.SEEDED) Random(craftSeed) else Random()
        fun mixed(selector:(MarketIngredient)->Int):Int{
            val values=inputs.map(selector)
            val average=values.sum()/values.size
            val strongest=values.maxOrNull()?:0
            return (average*0.55+strongest*0.45+random.nextInt(21)-10).roundToInt().coerceIn(0,100)
        }
        val food=mixed{it.food};val hardness=mixed{it.hardness};val sharpness=mixed{it.sharpness}
        val nutrition=mixed{it.nutrition};val durability=mixed{it.durability};val conductivity=mixed{it.conductivity}
        val freshness=mixed{it.freshness};val quality=mixed{it.quality};val rarity=mixed{it.rarity}
        val kind=when{food>=60&&(hardness>=55||sharpness>=55||conductivity>=55)->MarketKind.HYBRID;food>=50->MarketKind.FOOD;else->MarketKind.ITEM}
        val first=inputs[random.nextInt(inputs.size)].name.replace(" ","")
        val second=inputs[random.nextInt(inputs.size)].name.replace(" ","")
        val suffix=when(kind){
            MarketKind.FOOD->listOf("Ration","Stew","Cake","Mash","Bite","Meal")
            MarketKind.ITEM->listOf("Rig","Plate","Cutter","Core","Tool","Part")
            MarketKind.HYBRID->listOf("Kit","Blend","Pack","Forge","Mix","Special")
        }[random.nextInt(6)]
        val name=(if(inputs.size==1) first else first.take(max(3,first.length/2))+second.takeLast(max(3,second.length/2)))+" "+suffix
        val sourceBase=inputs.sumOf{it.baseValue}
        val propertyWeight=(quality+rarity+durability+nutrition+hardness+sharpness+conductivity+freshness)/8.0
        val marketValue=max(1,(sourceBase*0.45+propertyWeight*0.9).roundToInt())
        val buys=max(1,(marketValue*0.70).roundToInt())
        val sells=max(buys+1,(marketValue*1.25).roundToInt())
        val out=GeneratedMarketItem(
            "GEN-"+System.currentTimeMillis().toString(36).uppercase()+"-"+generatedInventory.size,
            name,kind,food,hardness,sharpness,nutrition,durability,conductivity,freshness,quality,rarity,
            marketValue,buys,sells,inputs.map{it.id},craftMode,if(craftMode==CraftRandomMode.SEEDED)craftSeed else null
        )
        generatedInventory+=out;lastGenerated=out;clearCraftSlots()
        return out
    }

    fun sellGeneratedToMarket(index:Int):Boolean{
        val item=generatedInventory.getOrNull(index)?:return false
        if(marketCredits<item.marketBuysFor)return false
        marketCredits-=item.marketBuysFor
        playerCredits+=item.marketBuysFor
        generatedInventory.removeAt(index)
        return true
    }

    fun buyGeneratedFromMarket(index:Int,customerId:String):Boolean{
        val item=generatedInventory.getOrNull(index)?:return false
        val wallet=customerCredits[customerId]?:return false
        if(wallet<item.marketSellsFor)return false
        customerCredits[customerId]=wallet-item.marketSellsFor
        marketCredits+=item.marketSellsFor
        generatedInventory.removeAt(index)
        return true
    }

    fun playerBuyGenerated(index:Int):Boolean{
        val item=generatedInventory.getOrNull(index)?:return false
        if(playerCredits<item.marketSellsFor)return false
        playerCredits-=item.marketSellsFor
        marketCredits+=item.marketSellsFor
        generatedInventory.removeAt(index)
        return true
    }

    fun craft(stallId:String):Boolean{
        val stall=stalls[stallId]?:return false
        val recipe=recipes[stall.item]?:return false
        if(recipe.parts.any{(bins[it]?:0)<1})return false
        recipe.parts.forEach{bins[it]=(bins[it]?:0)-1};stall.stock++;return true
    }
    fun sell(stallId:String,customerId:String):Boolean{
        val stall=stalls[stallId]?:return false
        val recipe=recipes[stall.item]?:return false
        val wallet=customerCredits[customerId]?:return false
        if(stall.stock<1||wallet<recipe.saleValue)return false
        stall.stock--;customerCredits[customerId]=wallet-recipe.saleValue;marketCredits+=recipe.saleValue;return true
    }
}
