package vn.techres.line.data.model.friend

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class FriendContactFrom : Serializable {
    @JsonField(name = ["total_record"])
    var total_record: Int? = 0

    @JsonField(name = ["list"])
    var list = ArrayList<Friend>()
}