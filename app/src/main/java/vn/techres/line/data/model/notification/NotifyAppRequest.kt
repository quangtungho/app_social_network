package vn.techres.line.data.model.notification

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class NotifyAppRequest : Serializable{
    @JsonField(name = ["chat_token"])
    var chat_token: String? = ""
}