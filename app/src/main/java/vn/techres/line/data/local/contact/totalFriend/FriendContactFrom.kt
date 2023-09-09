package vn.techres.line.data.local.contact.totalFriend

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.friend.Friend
import java.io.Serializable

class FriendContactFrom : Serializable {
    @JsonField(name = ["total_record"])
    var total_record: Int? = 0

    @JsonField(name = ["list"])
    var list = ArrayList<Friend>()
}