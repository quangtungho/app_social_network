package vn.techres.line.data.model.chat.data

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.LinkMediaChat
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class LinkMediaChatData : Serializable {
    @JsonField(name = ["total_record"])
    var total_record : Int? = 0

    @JsonField(name = ["list"])
    var list = ArrayList<LinkMediaChat>()
}