package vn.techres.line.data.local.contact.friendrequestcontact

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.local.contact.totalFriend.TotalFriend
import vn.techres.line.data.model.response.BaseResponse

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class FriendRequestContactResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = TotalFriend()
}