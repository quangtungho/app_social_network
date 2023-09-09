package vn.techres.line.data.model.contact.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.contact.ContactChat
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ContactChatRequest : Serializable {
    @JsonField(name = ["list_user"])
    var list_user: ArrayList<ContactChat>? = ArrayList()
}