package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class GroupRequest: Serializable {
    @JsonField(name = ["members"])
    var members = ArrayList<Int>()

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

}