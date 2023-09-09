package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.DetailGroup
import vn.techres.line.data.model.response.BaseResponse

class DetailGroupResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data: DetailGroup? = null
}