package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.food.FoodPurcharePoint
import java.io.Serializable

class CartPointFoodTakeAway : Serializable {
    @JsonField(name = ["id_branch"])
    var id_branch : Int? = 0

    @JsonField(name = ["branch_name"])
    var branch_name : String? = ""

    @JsonField(name = ["branch_avatar"])
    var branch_avatar : String? = ""

    @JsonField(name = ["id_branch"])
    var food = ArrayList<FoodPurcharePoint>()
}