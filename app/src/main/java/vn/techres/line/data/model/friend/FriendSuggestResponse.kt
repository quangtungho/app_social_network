package vn.techres.line.data.model.friend

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.response.BaseResponse

class FriendSuggestResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<Friend>()
}