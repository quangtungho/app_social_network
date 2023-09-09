package vn.techres.line.interfaces

import vn.techres.line.data.model.food.FoodTakeAway

interface DetailBranchHandler {
    fun onDetailBranch(int: Int)
    fun onDetailDish(id : Int)
    fun onFoodsTakeAway(foodAwayTake : FoodTakeAway, position :Int)
    fun onIncrease()
    fun onDecrease()
    fun onDetailFood(foodAwayTake : FoodTakeAway, position :Int)
    fun addDish(foodAwayTake : FoodTakeAway, position :Int)
}