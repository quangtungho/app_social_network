package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class UpdateDetailGroupResponse : Serializable {
    @JsonField(name = ["name"])
    var name : String? = ""

    @JsonField(name = ["full_name"])
    var full_name : String? = ""

    @JsonField(name = ["group_id"])
    var group_id : String? = ""

    @JsonField(name = ["avatar"])
    var avatar : String? = ""

    @JsonField(name = ["background"])
    var background : String? = ""

    @JsonField(name = ["member_id"])
    var member_id : Int? = 0
}