package vn.techres.line.data.model.notification

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class NotifyChatRequest : Serializable {
    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["group_id"])
    var group_id: String? = ""
}