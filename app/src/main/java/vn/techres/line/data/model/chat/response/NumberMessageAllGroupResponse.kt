package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.NumberMessageAllGroup
import vn.techres.line.data.model.response.BaseResponse

class NumberMessageAllGroupResponse : BaseResponse()  {
    @JsonField(name = ["data"])
    var data = NumberMessageAllGroup()
}