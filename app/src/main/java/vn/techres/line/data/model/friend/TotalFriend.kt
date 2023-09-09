package vn.techres.line.data.model.friend

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class TotalFriend : Serializable {
    @JsonField(name = ["contact_from"])
    var contact_from  = FriendContactFrom()

    @JsonField(name = ["contact_to"])
    var contact_to  = FriendContactTo()

    @JsonField(name = ["friend"])
    var friend  = Friend()
}