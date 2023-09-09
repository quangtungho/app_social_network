package vn.techres.line.data.model.chat.request.group

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.TagName
import java.io.Serializable

class ReplyGroupRequest : Serializable {
    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["group_id"])
    var group_id: String? = ""

    @JsonField(name = ["message"])
    var message: String? = ""

    @JsonField(name = ["random_key"])
    var random_key: String? = ""

    @JsonField(name = ["message_type"])
    var message_type: String? = ""

    @JsonField(name = ["list_tag_name"])
    var list_tag_name = ArrayList<TagName>()

    @JsonField(name = ["key_message_error"])
    var key_message_error: String? = ""
}