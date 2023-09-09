package vn.techres.line.data.model.reaction

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.response.BaseResponse

class InteractiveUserResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<InteractiveUser>()
}