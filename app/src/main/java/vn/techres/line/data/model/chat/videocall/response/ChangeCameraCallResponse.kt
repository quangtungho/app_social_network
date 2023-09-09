package vn.techres.line.data.model.chat.videocall.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ChangeCameraCallResponse : Serializable {
    @JsonField(name = ["member_id"])
    var member_id :Int? = 0
}