package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class FileChat : Serializable {
    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["link"])
    var link: String? = ""
}