package vn.techres.line.data.model.newfeed

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class YouTube : Serializable {

    @JsonField(name = ["videoId"])
    var videoId: String? = ""

    @JsonField(name = ["currentSecond"])
    var currentSecond: Float? = 0f

}