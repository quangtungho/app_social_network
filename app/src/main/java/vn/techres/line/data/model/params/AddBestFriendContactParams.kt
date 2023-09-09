package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.friend.request.AddBestFriendRequest
import vn.techres.line.data.model.request.BaseRequest

@JsonObject
class AddBestFriendContactParams : BaseRequest() {
    var params = AddBestFriendRequest()
}