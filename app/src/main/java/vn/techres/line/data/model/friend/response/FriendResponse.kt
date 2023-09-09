package vn.techres.line.data.model.friend.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.friend.data.FriendData
import vn.techres.line.data.model.response.BaseResponse

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class FriendResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = FriendData()
}