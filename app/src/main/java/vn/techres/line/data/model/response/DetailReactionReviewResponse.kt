package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.newfeed.DetailReactionReviewData

class DetailReactionReviewResponse: BaseResponse() {
    @JsonField(name = ["data"])
    var data = DetailReactionReviewData()
}