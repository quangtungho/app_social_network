package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.response.BaseResponse

class OneGroupResponse : BaseResponse()  {
    @JsonField(name = ["data"])
    var data = Group()
}