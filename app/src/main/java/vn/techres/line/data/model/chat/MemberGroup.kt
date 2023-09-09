package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class MemberGroup : Serializable {
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["members"])
    var members = ArrayList<Members>()
}