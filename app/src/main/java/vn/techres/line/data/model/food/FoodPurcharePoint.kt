package vn.techres.line.data.model.food

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable
import java.math.BigDecimal

@JsonObject
class FoodPurcharePoint : Serializable {
    @JsonField(name = ["id"])
    var id: Int = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["description"])
    var description : String? = ""

    @JsonField(name = ["price"])
    var price  : BigDecimal? = BigDecimal.ZERO

    @JsonField(name = ["imgage_logo_url"])
    var imgage_logo_url  : String? = ""

    @JsonField(name = ["food_image"])
    var food_image  : Avatar? = null

    @JsonField(name = ["point_to_purchase"])
    var point_to_purchase  : Int? = 0

    @JsonField(name = ["is_use_point"])
    var is_use_point : Int? = 1

    @JsonField(name = ["quantity"])
    var quantity : Int = 0

    @JsonField(name = ["note"])
    var note : String? = ""

}