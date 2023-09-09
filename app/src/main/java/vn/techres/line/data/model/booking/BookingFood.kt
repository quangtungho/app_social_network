package vn.techres.line.data.model.booking

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BookingFood: Serializable {
    @JsonField(name = ["name"])
    var name : String? = ""

    @JsonField(name = ["avatar"])
    var avatar : Avatar? = null

    @JsonField(name = ["quantity"])
    var quantity : Double? = 0.0

    @JsonField(name = ["price"])
    var price : Double? = 0.0

    @JsonField(name = ["food_id"])
    var food_id : Int? = 0

    @JsonField(name = ["total_amount"])
    var total_amount : Double? = 0.0

    @JsonField(name = ["is_confirm"])
    var is_confirm : Int? = 0

    @JsonField(name = ["is_gift"])
    var is_gift : Int? = 0
}