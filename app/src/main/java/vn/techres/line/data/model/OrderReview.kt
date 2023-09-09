package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable

@JsonObject
class OrderReview  : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["payment_date"])
    var payment_date: String? = ""

    @JsonField(name = ["total_amount"])
    var total_amount: Double? = 0.0

    @JsonField(name = ["membership_point_added"])
    var membership_point_added: Int? = 0

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String? = ""

    @JsonField(name = ["restaurant_avatar"])
    var restaurant_avatar: Avatar? = null

    @JsonField(name = ["branch_name"])
    var branch_name: String? = ""

    @JsonField(name = ["branch_avatar"])
    var branch_avatar: String? = ""
}