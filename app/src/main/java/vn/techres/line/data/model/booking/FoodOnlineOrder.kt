package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class FoodOnlineOrder : Serializable {

    @JsonField(name = ["food_id"])
    var food_id: Int? = 0

    @JsonField(name = ["quantity"])
    var quantity: Int? = 0

    @JsonField(name = ["is_use_point"])
    var is_use_point: Int? = 0

    @JsonField(name = ["note"])
    var note: String? = ""
}