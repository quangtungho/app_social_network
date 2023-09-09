package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.data.GroupData
import vn.techres.line.data.model.response.BaseResponse

class GroupsResponse : BaseResponse() {
    @JsonField(name = ["data"])
     var data = GroupData()
}