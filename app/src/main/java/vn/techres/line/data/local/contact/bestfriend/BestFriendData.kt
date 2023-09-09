package vn.techres.line.data.local.contact.bestfriend

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.friend.Friend

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BestFriendData {
    @JsonField(name = ["limit"])
    var limit : Int? = 0

    @JsonField(name = ["total_record"])
    var total_record : Int? = 0

    @JsonField(name = ["list"])
    var list = ArrayList<Friend>()
}