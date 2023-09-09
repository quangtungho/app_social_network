package vn.techres.line.data.model.response.game

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Avatar

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class UserWinner {
    @JsonField(name = ["full_name"])
    var full_name: String = ""

    @JsonField(name = ["phone"])
    var phone: String = ""

    @JsonField(name = ["total_win_times"])
    var total_win_times: Int = 0

    @JsonField(name = ["avatar"])
    var avatar: Avatar? = null

    @JsonField(name = ["gift_name"])
    var gift_name: String? = ""

    @JsonField(name = ["created_at"])
    var created_at: String = ""
}