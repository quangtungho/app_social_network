package vn.techres.line.data.model.chat.request.personal

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CreateGroupPersonalRequest : Serializable {

    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["admin_id"])
    var admin_id: Int? = 0
}