package vn.techres.line.data.model.chat.videocall.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ConnectCallPersonalResponse : Serializable {
    @JsonField(name = ["avatar"])
    var avatar : Avatar? = null

    @JsonField(name = ["full_name"])
    var full_name : String? = ""

    @JsonField(name = ["member_id"])
    var member_id : Int? = 0

    @JsonField(name = ["group_id"])
    var group_id : String? = ""

    @JsonField(name = ["key_room"])
    var key_room : String? = ""

    @JsonField(name = ["message_type"])
    var message_type : String? = ""

    @JsonField(name = ["type"])
    var type : String? = ""

    @JsonField(name = ["description"])
    var description : String? = ""
}