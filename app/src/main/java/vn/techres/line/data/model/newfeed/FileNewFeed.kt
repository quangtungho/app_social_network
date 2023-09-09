package vn.techres.line.data.model.newfeed

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class FileNewFeed : Serializable {
    @JsonField(name = ["type"])
    var type: String? = ""

    @JsonField(name = ["link"])
    var link: String? = ""
}