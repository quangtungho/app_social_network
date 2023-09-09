package vn.techres.line.data.model.friend.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.friend.NumberMakeFriend
import vn.techres.line.data.model.response.BaseResponse

class NumberMakeFriendResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = NumberMakeFriend()
}