package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.data.line.model.PostReview

class DetailCommentResponse: BaseResponse() {
    @JsonField(name = ["data"])
    var data =  PostReview()
}