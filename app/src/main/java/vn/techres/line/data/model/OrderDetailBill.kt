package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class OrderDetailBill {
    @JsonField(name = ["food_name"])
    var food_name: String? = ""

    @JsonField(name = ["order_status_name"])
    var order_status_name: String? = ""

    @JsonField(name = ["quantity"])
    var quantity: Double? = 0.0

    @JsonField(name = ["vat_percent"])
    var vat_percent: Double? = 0.0

    @JsonField(name = ["vat_amount"])
    var vat_amount: Int? = 0

    @JsonField(name = ["note"])
    var note: String? = ""

    @JsonField(name = ["price"])
    var price: BigDecimal? = BigDecimal.ZERO

    @JsonField(name = ["total_amount"])
    var total_amount: BigDecimal? = BigDecimal.ZERO

    @JsonField(name = ["order_status"])
    var order_status: Int? = 0

    @JsonField(name = ["is_purchase_by_point"])
    var is_purchase_by_point: Int? = 0

    @JsonField(name = ["point_to_purchase"])
    var point_to_purchase: Int? = 0

    @JsonField(name = ["total_point_to_purchase"])
    var total_point_to_purchase: Int? = 0

    @JsonField(name = ["category_type"])
    var category_type: Int? = 0

    @JsonField(name = ["approved_drink"])
    var approved_drink: Int? = 0

    @JsonField(name = ["buy_with_point"])
    var buy_with_point: Int? = 0

    @JsonField(name = ["is_gift"])
    var is_gift: Int? = 0
}