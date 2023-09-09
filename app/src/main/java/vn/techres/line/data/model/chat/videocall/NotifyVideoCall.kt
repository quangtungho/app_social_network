package vn.techres.line.data.model.chat.videocall

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class NotifyVideoCall : Serializable {
    @JsonField(name = ["avatar"])
    var avatar : String? = ""

    @JsonField(name = ["full_name"])
    var full_name : String? = ""

    @JsonField(name = ["user_id"])
    var user_id : Int? = 0

    @JsonField(name = ["conversation_type"])
    var conversation_type : String? = ""

    @JsonField(name = ["type_message"])
    var type_message : String? = ""

    @JsonField(name = ["group_id"])
    var group_id : String? = ""

    @JsonField(name = ["room_id"])
    var room_id : String? = ""

    @JsonField(name = ["call_status"])
    var call_status : String? = ""

    @JsonField(name = ["type"])
    var type : Int? = 0

    @JsonField(name = ["sound"])
    var sound : String? = ""

    @JsonField(name = ["option"])
    var option : Int? = 0

    @JsonField(name = ["group_id_for_notification"])
    var group_id_for_notification : Int? = 0

    @JsonField(name = ["call_member_create"])
    var call_member_create : Int? = 0
}