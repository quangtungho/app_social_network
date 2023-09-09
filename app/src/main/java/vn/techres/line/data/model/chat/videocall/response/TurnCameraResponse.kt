package vn.techres.line.data.model.chat.videocall.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class TurnCameraResponse : Serializable {
    @JsonField(name = ["status"])
    var status : Int? = 0
}