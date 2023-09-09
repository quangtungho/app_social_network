package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class SliderBanner : Serializable {
    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["link_banner"])
    var link_banner: String? = ""

    @JsonField(name = ["link"])
    var link: String? = ""

    @JsonField(name = ["imageUrl"])
    var imageUrl: String? = ""
}