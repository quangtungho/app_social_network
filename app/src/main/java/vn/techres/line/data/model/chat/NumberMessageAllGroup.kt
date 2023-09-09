package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class NumberMessageAllGroup : Serializable {
    @JsonField(name = ["number"])
    var number : Int? = 0
}