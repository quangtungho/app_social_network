package vn.techres.line.interfaces

import vn.techres.line.data.model.restaurant.RestaurantCard

interface LickItem {
    fun onClickItem(restaurant : RestaurantCard)
}