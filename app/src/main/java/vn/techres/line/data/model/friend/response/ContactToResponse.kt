package vn.techres.line.data.model.friend.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.friend.data.ContactToData
import vn.techres.line.data.model.response.BaseResponse

class ContactToResponse : BaseResponse() {
    @JsonField(name=["data"])
    var data = ContactToData()
}