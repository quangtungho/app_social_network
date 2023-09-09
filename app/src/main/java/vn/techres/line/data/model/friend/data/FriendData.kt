package vn.techres.line.data.model.friend.data

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.friend.Friend
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class FriendData : Serializable {
    @JsonField(name = ["limit"])
    var limit : Int? = 0

    @JsonField(name = ["total_record"])
    var total_record : Int? = 0

    @JsonField(name = ["list"])
    var list = ArrayList<Friend>()
}