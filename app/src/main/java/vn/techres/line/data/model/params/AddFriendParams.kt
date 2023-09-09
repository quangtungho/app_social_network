package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.request.BaseRequest

@JsonObject
class AddFriendParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = AddFriendRequest()
}