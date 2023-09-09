package vn.techres.line.interfaces.food

import vn.techres.line.data.model.food.FoodTakeAway

interface FoodTakeAwayDetailHandler {
    fun onChooseFood(food: FoodTakeAway)
}