package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class Logo  : Serializable {
    @JsonField(name = ["original"])
    var original : String? = ""

    @JsonField(name = ["medium"])
    var medium : String? = ""

    @JsonField(name = ["thumb"])
    var thumb : String? = ""
}