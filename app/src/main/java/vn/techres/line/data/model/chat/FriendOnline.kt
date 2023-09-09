package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class FriendOnline : Serializable {
    @JsonField(name = ["_id"])
    var _id : String? = ""

    @JsonField(name = ["user_id"])
    var user_id : Int? = 0

    @JsonField(name = ["full_name"])
    var full_name : String? = ""

    @JsonField(name = ["status"])
    var status : Int? = 0

    @JsonField(name = ["avatar"])
    var avatar : String? = ""
}