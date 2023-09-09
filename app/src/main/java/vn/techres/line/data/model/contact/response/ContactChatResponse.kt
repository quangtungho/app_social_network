package vn.techres.line.data.model.contact.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.contact.ContactNodeChat
import vn.techres.line.data.model.response.BaseResponse
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ContactChatResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<ContactNodeChat>()
}