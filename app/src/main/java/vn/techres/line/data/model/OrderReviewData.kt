package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class OrderReviewData : Serializable {
    @JsonField(name = ["limit"])
    var limit: Int? = 0

    @JsonField(name = ["list"])
    var list =  ArrayList<OrderReview>()

    @JsonField(name = ["total_record"])
    var total_record: Int? = 0
}