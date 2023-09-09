package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.BranchGift

class BranchGiftResponse: BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<BranchGift>()
}