package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class ListReviewBranchRequest {
    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["customer_id"])
    var customer_id: Int? = 0

    @JsonField(name = ["types"])
    var types: String? = ""

    @JsonField(name = ["branch_review_status"])
    var branch_review_status: Int = 0

    @JsonField(name = ["limit"])
    var limit: Int = 0

    @JsonField(name = ["page"])
    var page: Int = 0
}