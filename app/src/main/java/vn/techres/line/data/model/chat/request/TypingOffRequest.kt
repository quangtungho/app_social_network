package vn.techres.line.data.model.chat.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class TypingOffRequest : Serializable {
    @JsonField(name = ["group_id"])
    var group_id: String? = ""

    @JsonField(name = ["member_id"])
    var member_id: Int? = 0
}