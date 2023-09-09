package vn.techres.line.data.model.chat.request

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.Sender
import java.io.Serializable

class RemoveGroupParty : Serializable {
    @JsonField(name = ["sender"])
    var sender = Sender()

    @JsonField(name = ["group_id"])
    var group_id : String? = ""
}