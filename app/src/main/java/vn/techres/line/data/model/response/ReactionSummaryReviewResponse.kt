package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.ReactionSummaryReview

class ReactionSummaryReviewResponse: BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<ReactionSummaryReview>()
}