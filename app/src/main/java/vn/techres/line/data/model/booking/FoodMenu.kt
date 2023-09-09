package vn.techres.line.data.model.booking

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable
import java.math.BigDecimal

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class FoodMenu : Serializable {


    @JsonField(name = ["id"])
    var id: Int = 0

    @JsonField(name = ["avatar"])
    var avatar: Avatar? = null

    @JsonField(name = ["prefix"])
    var prefix: String? = null

    @JsonField(name = ["description"])
    var description: String? = null

    @JsonField(name = ["price"])
    var price: BigDecimal? = BigDecimal.ONE

    @JsonField(name = ["food_name"])
    var food_name: String? = null

    @JsonField(name = ["normalize_name"])
    var normalize_name: String? = null

    @JsonField(name = ["is_sell_by_weight"])
    var is_sell_by_weight: Int = 0

    @JsonField(name = ["is_bbq"])
    var is_bbq: Int = 0

    @JsonField(name = ["isSelected"])
    var isSelected: Int = 0

    @JsonField(name = ["quantity"])
    var quantity: Int = 0

    @JsonField(name = ["note"])
    var note: String? = null


}