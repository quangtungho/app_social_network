package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ChatTextResponse : Serializable {

    @JsonField(name = ["groupName"])
    var groupName: String? = ""

    @JsonField(name = ["currentGroupId"])
    var currentGroupId: String? = ""

    @JsonField(name = ["currentUserId"])
    var currentUserId: String? = ""

    @JsonField(name = ["message"])
    var message: String? = ""

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["messageType"])
    var messageType: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["updatedAt"])
    var updatedAt: String? = ""

    @JsonField(name = ["cunrrentId"])
    var cunrrentId: String? = ""
}