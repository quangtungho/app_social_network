package vn.techres.line.data.model.chat.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class UpdateGroupRequest : Serializable {
    @JsonField(name = ["name"])
    var name : String? = ""

    @JsonField(name = ["avatar"])
    var avatar : String? = ""

    @JsonField(name = ["background"])
    var background : String? = ""
}