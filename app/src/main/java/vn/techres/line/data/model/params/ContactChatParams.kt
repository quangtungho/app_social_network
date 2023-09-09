package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.contact.request.ContactChatRequest
import vn.techres.line.data.model.request.BaseRequest

class ContactChatParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = ContactChatRequest()
}