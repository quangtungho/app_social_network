package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField

class UpdateAvatarRequest {
    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["chat_token"])
    var chat_token: String? = ""
}