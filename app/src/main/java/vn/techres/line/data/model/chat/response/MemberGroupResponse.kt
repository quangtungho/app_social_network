package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.MemberGroup
import vn.techres.line.data.model.response.BaseResponse

class MemberGroupResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = MemberGroup()
}