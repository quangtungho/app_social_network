package vn.techres.line.data.model.chat.request.group

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.FileNodeJs
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ChatAudioGroupRequest : Serializable {
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

}