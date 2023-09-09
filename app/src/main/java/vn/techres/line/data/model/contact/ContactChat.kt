package vn.techres.line.data.model.contact

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ContactChat() : Serializable {
    @JsonField(name = ["contact_id"])
    var contact_id: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["phone"])
    var phone: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: String? = ""


    constructor(contact_id: Int?, full_name: String?, phone: String?, avatar: String?) : this(){
        this.contact_id = contact_id
        this.full_name = full_name
        this.phone = phone
        this.avatar = avatar
    }
}