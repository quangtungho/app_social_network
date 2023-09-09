package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class MessageNotSeenResponse : Serializable {
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["background"])
    var background: String? = ""

    @JsonField(name = ["number"])
    var number: Int? = 0

    @JsonField(name = ["last_message"])
    var last_message: String? = ""

    @JsonField(name = ["last_message_type"])
    var last_message_type: String? = ""

    @JsonField(name = ["user_name_last_message"])
    var user_name_last_message: String? = ""

    @JsonField(name = ["status_last_message"])
    var status_last_message: Int? = 0

    @JsonField(name = ["user_last_message_id"])
    var user_last_message_id: Int? = 0

    @JsonField(name = ["created_last_message"])
    var created_last_message: String? = ""
}