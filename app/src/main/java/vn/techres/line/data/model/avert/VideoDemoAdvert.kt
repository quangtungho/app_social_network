package vn.techres.line.data.model.avert

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class VideoDemoAdvert : Serializable {
    @JsonField(name = ["url"])
    var url: String? = ""

    @JsonField(name = ["auto"])
    var auto: Int? = 0
}