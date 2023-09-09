package vn.techres.line.data.model.voucher

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class VoucherDetail : Serializable {

    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["customer_id"])
    var customer_id: Int? = 0

    @JsonField(name = ["customer_name"])
    var customer_name: String? = ""

    @JsonField(name = ["customer_phone"])
    var customer_phone: String? = ""

    @JsonField(name = ["promotion_campaign_id"])
    var promotion_campaign_id: Int? = 0

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String? = ""

    @JsonField(name = ["restaurant_brand_id"])
    var restaurant_brand_id: Int? = 0

    @JsonField(name = ["restaurant_brand_name"])
    var restaurant_brand_name: String? = ""

    @JsonField(name = ["restaurant_voucher_id"])
    var restaurant_voucher_id: Int? = 0

    @JsonField(name = ["is_used"])
    var is_used: Int? = 0

    @JsonField(name = ["restaurant_voucher_code"])
    var restaurant_voucher_code: String? = ""

    @JsonField(name = ["is_expired"])
    var is_expired: Int? = 0

    @JsonField(name = ["promotion_campaign_description"])
    var promotion_campaign_description: String? = ""

    @JsonField(name = ["restaurant_object_promotion_campaign_id"])
    var restaurant_object_promotion_campaign_id: Int? = 0

    @JsonField(name = ["banner_image_url"])
    var banner_image_url: String? = ""

    @JsonField(name = ["discount_percent"])
    var discount_percent: Int? = 0

    @JsonField(name = ["restaurant_promotion"])
    var restaurant_promotion = RestaurantPromotion()

    @JsonField(name = ["created_at"])
    var created_at: String? = ""
}