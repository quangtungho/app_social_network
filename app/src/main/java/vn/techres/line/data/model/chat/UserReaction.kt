package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.utils.Avatar

class UserReaction {
    @JsonField(name = ["member_id"])
    var member_id = 0

    @JsonField(name = ["avatar"])
    var avatar: Avatar? = null

    @JsonField(name = ["love"])
    var love: Int? = 0

    @JsonField(name = ["smile"])
    var smile: Int? = 0

    @JsonField(name = ["like"])
    var like: Int? = 0

    @JsonField(name = ["angry"])
    var angry: Int? = 0

    @JsonField(name = ["sad"])
    var sad: Int? = 0

    @JsonField(name = ["wow"])
    var wow: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name = ""

    @JsonField(name = ["reactions_type"])
    var reactions_type = 0

    @JsonField(name = ["total_reaction"])
    var total_reaction = 0
}