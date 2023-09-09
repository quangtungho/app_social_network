package vn.techres.line.data.model.contact

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ContactNodeChat : Serializable {

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["phone"])
    var phone: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: Avatar? = null

    @JsonField(name = ["color"])
    var color: Int = 0

    @JsonField(name = ["isCheck"])
    var isCheck: Boolean = false
}