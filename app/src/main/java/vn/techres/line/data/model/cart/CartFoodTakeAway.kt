package vn.techres.line.data.model.cart

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.food.FoodTakeAway
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CartFoodTakeAway : Serializable {

    @JsonField(name = ["id_branch"])
    var id_branch : Int? = 0

    @JsonField(name = ["branch_name"])
    var branch_name : String? = ""

    @JsonField(name = ["branch_avatar"])
    var branch_avatar : String? = ""

    @JsonField(name = ["id_branch"])
    var food = ArrayList<FoodTakeAway>()
}