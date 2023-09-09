package vn.techres.line.data.model.chat.videocall.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class RejectVideoCallRequest : Serializable {
    @JsonField(name = ["sender_id"])
    var sender_id : Int? = 0

    @JsonField(name = ["receiver_id"])
    var receiver_id : Int? = 0

    @JsonField(name = ["group_id"])
    var group_id : String? = ""
}