package vn.techres.line.data.model.chat.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.FileNodeJs
import vn.techres.line.data.model.chat.Reply
import vn.techres.line.data.model.chat.TagName
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ReplyRequest : Serializable {
    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["message_type"])
    var message_type: String? = ""

    @JsonField(name = ["group_id"])
    var group_id: String? = ""

    @JsonField(name = ["message"])
    var message: String? = ""

    @JsonField(name = ["file"])
    var file = ArrayList<FileNodeJs>()

    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["group_name"])
    var group_name: String? = ""

    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["message_reply "])
    var message_reply = Reply()

    @JsonField(name = ["tag_name"])
    var tag_name: Int? = 0

    @JsonField(name = ["list_tag_name"])
    var list_tag_name = ArrayList<TagName>()

}