package vn.techres.line.data.local.contact.newfriend

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.response.BaseResponse

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class NewFriendResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = NewFriendData()
}