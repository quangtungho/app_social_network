package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class GroupsRequest: Serializable {
    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["limit"])
    var limit:Int? = 0

}