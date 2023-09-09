package vn.techres.line.data.local.contact.totalFriend

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class TotalFriend : Serializable {
    @JsonField(name = ["contact_from"])
    var contact_from  = FriendContactFrom()
}