package vn.techres.line.data.model.branch.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.branch.ListNewBranch
import vn.techres.line.data.model.response.BaseResponse

class ListNewBranchResponse: BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<ListNewBranch>()
}