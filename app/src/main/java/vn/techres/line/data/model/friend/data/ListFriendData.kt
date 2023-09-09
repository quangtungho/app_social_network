package vn.techres.line.data.model.friend.data

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.friend.Friend
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ListFriendData : Serializable {
    @JsonField(name = ["list_friends"])
    var list_friends = ArrayList<Friend>()
}