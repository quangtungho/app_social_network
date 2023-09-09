package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.FriendOnline
import vn.techres.line.data.model.response.BaseResponse

class FriendOnlineResponse : BaseResponse() {
    @JsonField(name = ["list_friends"])
    var list_friends = ArrayList<FriendOnline>()
}