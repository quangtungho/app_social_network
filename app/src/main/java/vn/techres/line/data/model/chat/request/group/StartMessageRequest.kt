package vn.techres.line.data.model.chat.request.group

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class StartMessageRequest : Serializable {
    @JsonField(name = ["member_id"])
    var member_id : Int? = 0


    @JsonField(name = ["group_id"])
    var group_id : String? = ""

    @JsonField(name = ["random_key"])
    var random_key : String? = ""

    @JsonField(name = ["key_message_error"])
    var key_message_error : String? = ""
}