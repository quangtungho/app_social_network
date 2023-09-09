package vn.techres.line.interfaces

import vn.techres.line.data.model.ItemCart
import vn.techres.line.data.model.food.FoodTakeAway

interface EditItemCartHandler {
    fun onChooseItemCart(itemCart: ItemCart)
    fun onChangeItemCart(listCart: ArrayList<FoodTakeAway>)
}