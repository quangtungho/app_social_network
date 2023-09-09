package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.data.line.model.PostReview

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class DataPostedRequest : BaseRequest(){
    @JsonField(name = ["params"])
    var params = PostReview()
}