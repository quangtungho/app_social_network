package vn.techres.line.data.model.chat.videocall

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.Receiver
import vn.techres.line.data.model.chat.Sender
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class AcceptVideoCallRequest : Serializable {
    @JsonField(name = ["sender"])
    var sender = Sender()

    @JsonField(name = ["receiver"])
    var receiver = Receiver()

    @JsonField(name = ["group_id"])
    var group_id : String? = ""
}