package vn.techres.line.data.model.food

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.ServeTimes
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable

@JsonObject
class FoodTakeAway : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["avatar"])
    var avatar: Avatar? = null

    @JsonField(name = ["prefix"])
    var prefix: String? = ""

    @JsonField(name = ["description"])
    var description: String? = ""

    @JsonField(name = ["price"])
    var price: Float? = 0F

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["normalize_name"])
    var normalize_name: String? = ""

    @JsonField(name = ["is_sell_by_weight"])
    var is_sell_by_weight: Int? = 0

    @JsonField(name = ["normalize_name"])
    var is_bbq: Int? = 0

    @JsonField(name = ["quantity"])
    var quantity: Int = 0

    @JsonField(name = ["note"])
    var note: String? = ""

    @JsonField(name = ["branchStar"])
    var branchStar: Float? = 0F

    @JsonField(name = ["is_use_point"])
    var is_use_point: Int? = 0

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["serve_times"])
    var serve_times = ArrayList<ServeTimes>()

    @JsonField(name = ["branch_image_url"])
    var branch_image_urls: String? = ""


}