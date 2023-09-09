package vn.techres.line.data.local.contact.myfriend

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.response.BaseResponse

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class MyFriendResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = MyFriendData()
}