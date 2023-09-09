package vn.techres.line.data.model.chat.request.group

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.FileNodeJs
import java.io.Serializable

class ChatImageGroupRequest : Serializable {
    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["group_id"])
    var group_id: String? = ""

    @JsonField(name = ["files"])
    var files: ArrayList<FileNodeJs>? = ArrayList()

    @JsonField(name = ["message_type"])
    var message_type: String? = ""

    @JsonField(name = ["key_message_error"])
    var key_message_error: String? = ""

    @JsonField(name = ["key_message"])
    var key_message: String? = ""

}