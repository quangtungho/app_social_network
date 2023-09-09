package vn.techres.line.data.model.friend.data

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.friend.Friend
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ContactFromData : Serializable {
    @JsonField(name = ["contact_from"])
    var contact_from = ArrayList<Friend>()
}