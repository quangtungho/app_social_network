package vn.techres.line.interfaces

import vn.techres.line.data.model.booking.FoodMenu

interface ChooseMenuFoodHandler {
    fun onPlusQuality(food : FoodMenu)
    fun onMinusQuality(food : FoodMenu)
    fun onPlusQualityCart(position : Int)
    fun onMinusQualityCart(position : Int)
    fun onRemoveFood(food : FoodMenu)
    fun onRemoveFoodCart(position : Int)
}