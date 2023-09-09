package vn.techres.line.data.model.friend.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.friend.data.ContactFromData
import vn.techres.line.data.model.response.BaseResponse

class ConTactFromResponse : BaseResponse() {
    @JsonField(name=["data"])
    var data = ContactFromData()
}