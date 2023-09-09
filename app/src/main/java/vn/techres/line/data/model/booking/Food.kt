package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable

@JsonObject
class Food : Serializable {
    @JsonField(name = ["id"])
    var id: Int = 0

    @JsonField(name = ["is_sell_by_weight"])
    var is_sell_by_weight: Int = 0

    @JsonField(name = ["is_bbq"])
    var is_bbq: Int = 0

    @JsonField(name = ["is_out_stock"])
    var is_out_stock: Int = 0

    @JsonField(name = ["is_allow_purchase_by_point"])
    var is_allow_purchase_by_point: Int = 0

    @JsonField(name = ["avatar"])
    var avatar = Avatar()

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["prefix"])
    var prefix: String? = ""

    @JsonField(name = ["food_name"])
    var food_name: String? = ""

    @JsonField(name = ["normalize_name"])
    var normalize_name: String? = ""

    @JsonField(name = ["description"])
    var description: String? = ""

    @JsonField(name = ["quantity"])
    var quantity: Double = 0.0

    @JsonField(name = ["price"])
    var price: Double = 0.0

    @JsonField(name = ["total_amount"])
    var total_amount: Double = 0.0
}