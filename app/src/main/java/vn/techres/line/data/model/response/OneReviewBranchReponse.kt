package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.data.line.model.PostReview

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class OneReviewBranchReponse: BaseResponse(){
    @JsonField(name = ["data"])
    var data = PostReview()
}