package vn.techres.line.data.model.friend.request

import com.bluelinelabs.logansquare.annotation.JsonField

class AddBestFriendRequest {
    @JsonField(name = ["list_user_ids"])
    var list_user_ids = ArrayList<Int>()

    @JsonField(name = ["type"])
    var type: Int? = 0
}