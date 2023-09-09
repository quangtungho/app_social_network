package vn.techres.line.interfaces

import vn.techres.line.data.model.food.FoodPurcharePoint

interface FoodsPointHandler {
    fun onClickShow(position: Int, foodPurcharePoint: FoodPurcharePoint)
}