package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.Sender
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ListMessageViewedResponse : Serializable {
    @JsonField(name = ["avatar"])
    var avatar : Sender? = null
}