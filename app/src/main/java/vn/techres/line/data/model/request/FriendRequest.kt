package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class FriendRequest : Serializable {
    @JsonField(name=["contact_to_user_id"])
    var contact_to_user_id: Int? = 0

    @JsonField(name=["contact_from_user_id"])
    var contact_from_user_id: Int? = 0

    @JsonField(name=["friend_user_id"])
    var friend_user_id: Int? = 0
}