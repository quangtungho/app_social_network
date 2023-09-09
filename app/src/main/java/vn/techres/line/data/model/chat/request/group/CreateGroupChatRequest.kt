package vn.techres.line.data.model.chat.request.group

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class CreateGroupChatRequest : Serializable {
    @JsonField(name = ["members"])
    var members = ArrayList<Int>()

    @JsonField(name = ["group_id"])
    var group_id : String? = ""
}