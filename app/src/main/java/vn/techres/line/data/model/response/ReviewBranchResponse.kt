package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.ReviewBranchData

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ReviewBranchResponse: BaseResponse(){
    @JsonField(name = ["data"])
    var data = ReviewBranchData()
}