package vn.techres.line.data.local.contact.bestfriend

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.response.BaseResponse

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BestFriendResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = BestFriendData()
}