package vn.techres.line.data.model.friend.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.friend.data.FriendData
import vn.techres.line.data.model.response.BaseResponse

class SearchFriendResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = FriendData()
}