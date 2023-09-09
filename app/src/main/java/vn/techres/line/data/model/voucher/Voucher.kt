package vn.techres.line.data.model.voucher

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Voucher : Serializable {

    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["promotion_campaign_id"])
    var promotion_campaign_id: Int? = 0

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["restaurant_voucher_id"])
    var restaurant_voucher_id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String? = ""

    @JsonField(name = ["restaurant_brand_id"])
    var restaurant_brand_id: Int? = 0

    @JsonField(name = ["restaurant_brand_name"])
    var restaurant_brand_name: String? = ""

    @JsonField(name = ["banner_image_url"])
    var banner_image_url: String? = ""

    @JsonField(name = ["code"])
    var code: String? = ""

    @JsonField(name = ["used_count"])
    var used_count: Int? = 0

    @JsonField(name = ["is_used"])
    var is_used: Int? = 0

    @JsonField(name = ["last_used_at"])
    var last_used_at: String? = ""

    @JsonField(name = ["is_expired"])
    var is_expired: Int? = 0

    @JsonField(name = ["promotion_campaign_name"])
    var promotion_campaign_name: String? = ""

    @JsonField(name = ["promotion_campaign_description"])
    var promotion_campaign_description: String? = ""

    @JsonField(name = ["discount_percent"])
    var discount_percent: Double? = 0.0

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["restaurant_promotion"])
    var restaurant_promotion = RestaurantPromotion()
}