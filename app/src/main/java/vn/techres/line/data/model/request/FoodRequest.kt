package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class FoodRequest : Serializable {
    @JsonField(name = ["food_id"])
    var food_id: Int = 0
    @JsonField(name = ["quantity"])
    var quantity: Int = 0
}