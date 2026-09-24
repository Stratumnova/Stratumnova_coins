package org.stratumnova.buildbycoin.runtime

data class Recipe(val item:String,val parts:List<String>,val saleValue:Int)
data class StallState(val id:String,val item:String,var stock:Int=0)
class MarketTestState{
    var marketCredits=0
    val customerCredits=linkedMapOf("CUSTOMER-1" to 30,"CUSTOMER-2" to 30,"CUSTOMER-3" to 30)
    val recipes=linkedMapOf(
        "ITEM-1" to Recipe("ITEM-1",listOf("PART-A","PART-B"),10),
        "ITEM-2" to Recipe("ITEM-2",listOf("PART-C","PART-D"),12),
        "ITEM-3" to Recipe("ITEM-3",listOf("PART-E","PART-F"),15)
    )
    val bins=linkedMapOf(
        "PART-A" to 3,"PART-B" to 3,"PART-C" to 3,"PART-D" to 3,"PART-E" to 3,"PART-F" to 3
    )
    val stalls=linkedMapOf(
        "STALL-1" to StallState("STALL-1","ITEM-1"),
        "STALL-2" to StallState("STALL-2","ITEM-2"),
        "STALL-3" to StallState("STALL-3","ITEM-3")
    )
    fun craft(stallId:String):Boolean{
        val stall=stalls[stallId]?:return false
        val recipe=recipes[stall.item]?:return false
        if(recipe.parts.any{(bins[it]?:0)<1})return false
        recipe.parts.forEach{bins[it]=(bins[it]?:0)-1}
        stall.stock++
        return true
    }
    fun sell(stallId:String,customerId:String):Boolean{
        val stall=stalls[stallId]?:return false
        val recipe=recipes[stall.item]?:return false
        val wallet=customerCredits[customerId]?:return false
        if(stall.stock<1||wallet<recipe.saleValue)return false
        stall.stock--;customerCredits[customerId]=wallet-recipe.saleValue;marketCredits+=recipe.saleValue
        return true
    }
}
