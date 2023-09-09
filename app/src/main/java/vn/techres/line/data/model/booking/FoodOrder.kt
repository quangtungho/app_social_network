package vn.techres.line.data.model.booking

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable
@JsonObject
class FoodOrder : Serializable {
    @JsonField(name = ["food_id"])
    var food_id: Int = 0
    @JsonField(name = ["quantity"])
    var quantity: Int = 0
}