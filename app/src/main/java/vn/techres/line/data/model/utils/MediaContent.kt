package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class MediaContent : Serializable {
    @JsonField(name = ["url"])
    var url: String = ""

    @JsonField(name = ["type"])
    var type: Int? = 0
}