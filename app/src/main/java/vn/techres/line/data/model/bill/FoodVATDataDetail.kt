package vn.techres.line.data.model.bill

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class FoodVATDataDetail : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["order_status"])
    var order_status: Int? = 0

    @JsonField(name = ["point_to_purchase"])
    var point_to_purchase: Int? = 0

    @JsonField(name = ["vat_amount"])
    var vat_amount: Int? = 0

    @JsonField(name = ["is_gift"])
    var is_gift: Int? = 0

    @JsonField(name = ["is_purchase_by_point"])
    var is_purchase_by_point: Int? = 0

    @JsonField(name = ["total_point_to_purchase"])
    var total_point_to_purchase: Int? = 0

    @JsonField(name = ["category_type"])
    var category_type: Int? = 0

    @JsonField(name = ["approved_drink"])
    var approved_drink: Int? = 0

    @JsonField(name = ["restaurant_promotion_id"])
    var restaurant_promotion_id: Int? = 0

    @JsonField(name = ["is_promotion"])
    var is_promotion: Int? = 0

    @JsonField(name = ["price"])
    var price: Double? = 0.0

    @JsonField(name = ["vat_percent"])
    var vat_percent: Double? = 0.0

    @JsonField(name = ["total_amount"])
    var total_amount: Double? = 0.0

    @JsonField(name = ["quantity"])
    var quantity: Double? = 0.0

    @JsonField(name = ["note"])
    var note: String? = ""

    @JsonField(name = ["food_name"])
    var food_name: String? = ""

    @JsonField(name = ["order_status_name"])
    var order_status_name: String? = ""
}