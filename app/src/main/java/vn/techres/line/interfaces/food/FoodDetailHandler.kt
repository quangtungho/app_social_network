package vn.techres.line.interfaces.food

import vn.techres.line.data.model.food.FoodTakeAway

interface FoodDetailHandler {
    fun onChooseFoodSuggest(food : FoodTakeAway)
}