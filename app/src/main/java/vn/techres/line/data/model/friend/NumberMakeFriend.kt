package vn.techres.line.data.model.friend

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class NumberMakeFriend : Serializable {
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["count"])
    var count: Int? = 0
}