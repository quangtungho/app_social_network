package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Media : Serializable {
    @JsonField(name = ["fileName"])
    var fileName: String = ""

    @JsonField(name = ["nameFile"])
    var url = Url()

    @JsonField(name = ["original"])
    var original : String? = ""

    @JsonField(name = ["medium"])
    var medium : String? = ""

    @JsonField(name = ["thumb"])
    var thumb : String? = ""

    @JsonField(name = ["width"])
    var width: Int = 0

    @JsonField(name = ["height"])
    var height: Int = 0

    @JsonField(name = ["ratio"])
    var ratio: Float = 0f

    @JsonField(name = ["type"])
    var type: Int? = 0

    @JsonField(name = ["linkPath"])
    var typePath: Int? = 0
}