package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.branch.BranchDetail

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class DetailBranchResponse: BaseResponse(){
    @JsonField(name = ["data"])
    var data = BranchDetail()
}