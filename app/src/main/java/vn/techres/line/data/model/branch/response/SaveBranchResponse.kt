package vn.techres.line.data.model.branch.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.branch.data.SaveBranchData
import vn.techres.line.data.model.response.BaseResponse

class SaveBranchResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = SaveBranchData()
}