package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.Member
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class GroupPersonal : Serializable {
    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["messageNumber"])
    var messageNumber: Int? = 0

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["statusLastMessage"])
    var statusLastMessage: Int? = 0

    @JsonField(name = ["lastMessage"])
    var lastMessage: String? = ""

    @JsonField(name = ["userNameLastMessage"])
    var userNameLastMessage: String? = ""

    @JsonField(name = ["lastMessageType"])
    var lastMessageType: String? = ""

    @JsonField(name = ["createdLastMessageOld"])
    var createdLastMessageOld: String? = ""

    @JsonField(name = ["status_typing"])
    var status_typing: Int? = 0

    @JsonField(name = ["user_typing"])
    var user_typing: String? = ""

    @JsonField(name = ["number_by_member_id"])
    var number_by_member_id: Int? = 0

    @JsonField(name = ["conversationType"])
    var conversationType: String? = ""

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["prefix"])
    var prefix: String? = ""

    @JsonField(name = ["normalize_name"])
    var normalize_name: String? = ""

    @JsonField(name = ["prefix_two_persional"])
    var prefix_two_persional = ArrayList<String>()

    @JsonField(name = ["normalize_name_two_persional"])
    var normalize_name_two_persional = ArrayList<String>()

    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["userNumber"])
    var userNumber: Int? = 0

    @JsonField(name = ["members"])
    var members = ArrayList<Member>()

    @JsonField(name = ["createdAt"])
    var createdAt: String? = ""

//    @JsonField(name = ["updatedAt"])
//    var updatedAt: String? = ""

    @JsonField(name = ["__v"])
    var __v: Int? = 0
}