package vn.techres.line.data.model.voucher

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.branch.Branch
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class RestaurantPromotion : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["code"])
    var code: String? = ""

    @JsonField(name = ["branch"])
    var branch = ArrayList<Branch>()

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

    @JsonField(name = ["branch_ids"])
    var branch_ids = ArrayList<Int>()

    @JsonField(name = ["information"])
    var information: String? = ""

    @JsonField(name = ["banner_image_url"])
    var banner_image_url: String? = ""

    @JsonField(name = ["discount_percent"])
    var discount_percent: Double? = 0.0

    @JsonField(name = ["day_of_weeks"])
    var day_of_weeks = ArrayList<Int>()

    @JsonField(name = ["from_date"])
    var from_date: String? = ""

    @JsonField(name = ["to_date"])
    var to_date: String? = ""

    @JsonField(name = ["from_hour"])
    var from_hour: Int? = 0

    @JsonField(name = ["to_hour"])
    var to_hour: Int? = 0

    @JsonField(name = ["category_types"])
    var category_types = ArrayList<Int>()

    @JsonField(name = ["maximum_use_time_per_voucher"])
    var maximum_use_time_per_voucher: Int? = 0

    @JsonField(name = ["is_actived"])
    var is_actived: Int? = 0

    @JsonField(name = ["created_at"])
    var created_at: String? = ""
}