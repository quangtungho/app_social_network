package vn.techres.line.data.model.chat.videocall.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class AcceptVideoCallRequest : Serializable {
    @JsonField(name = ["member_id"])
    var member_id :Int? = 0

    @JsonField(name = ["key_room"])
    var key_room : String? = ""

    @JsonField(name = ["group_id"])
    var group_id : String? = ""

    @JsonField(name = ["message_type"])
    var message_type : String? = ""

    @JsonField(name = ["type"])
    var type : String? = ""

}