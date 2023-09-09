package vn.techres.line.data.model.friend.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class NumberMakeFriendRequest : Serializable {
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["count"])
    var count: Int? = 0
}