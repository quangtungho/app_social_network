package vn.techres.line.data.model.reaction

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable

class InteractiveUser : Serializable {
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["user_id"])
    var user_id: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["avatar"])
    var avatar = Avatar()

    @JsonField(name = ["phone"])
    var phone: String? = ""

    @JsonField(name = ["gender"])
    var gender: Int? = 0

    @JsonField(name = ["birthday"])
    var birthday: String? = ""

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["reaction_id"])
    var reaction_id: Int? = 0

}