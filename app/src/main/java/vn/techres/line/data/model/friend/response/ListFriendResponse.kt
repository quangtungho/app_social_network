package vn.techres.line.data.model.friend.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.friend.data.ListFriendData
import vn.techres.line.data.model.response.BaseResponse

class ListFriendResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ListFriendData()
}