package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class Url : Serializable {
    @JsonField(name = ["fileName"])
    var fileName : String? = ""

    @JsonField(name = ["original"])
    var original : String? = ""

    @JsonField(name = ["medium"])
    var medium : String? = ""

    @JsonField(name = ["thumb"])
    var thumb : String? = ""


}