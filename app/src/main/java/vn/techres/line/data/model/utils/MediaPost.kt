package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class MediaPost : Serializable {
    @JsonField(name = ["type"])
    var type: Int? = 0

    @JsonField(name = ["url"])
    var original: String = ""

    @JsonField(name = ["fileName"])
    var fileName: String = ""

    @JsonField(name = ["height"])
    var height: Int = 0

    @JsonField(name = ["width"])
    var width: Int = 0

    @JsonField(name = ["ratio"])
    var ratio: Float = 0f

    @JsonField(name = ["linkPath"])
    var typePath: Int? = 0
}