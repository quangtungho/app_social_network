package vn.techres.line.data.model.friend.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.friend.TotalFriend
import vn.techres.line.data.model.response.BaseResponse

class TotalFriendRequestResponse: BaseResponse() {
    @JsonField(name = ["data"])
    var data = TotalFriend()
}