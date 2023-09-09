package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class AddMember : Serializable {
    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name: String? = ""
}