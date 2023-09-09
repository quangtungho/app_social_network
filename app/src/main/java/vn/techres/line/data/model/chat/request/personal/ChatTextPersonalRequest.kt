package vn.techres.line.data.model.chat.request.personal

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class ChatTextPersonalRequest : Serializable {

    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["group_id"])
    var group_id: String? = ""

    @JsonField(name = ["message"])
    var message: String? = ""

    @JsonField(name = ["message_type"])
    var message_type: String? = ""

    @JsonField(name = ["key_message_error"])
    var key_message_error: String? = ""
}