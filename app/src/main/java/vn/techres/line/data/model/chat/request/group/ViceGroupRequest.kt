package vn.techres.line.data.model.chat.request.group

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ViceGroupRequest : Serializable {
    @JsonField(name = ["member_id"])
    var member_id : Int? = 0
}