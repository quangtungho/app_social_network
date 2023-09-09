package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField

class DraftPostRequest {
    @JsonField(name = ["branch_id"])
    var branch_id: Int? = -1

    @JsonField(name = ["branch_review_status"])
    var branch_review_status: Int? = 0

    @JsonField(name = ["customer_id"])
    var customer_id: Int? = -1

    @JsonField(name = ["limit"])
    var limit: Int? = 50

    @JsonField(name = ["page"])
    var page: Int? = 1

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = -1

    @JsonField(name = ["types"])
    var types: String? = "0,1,3"

}