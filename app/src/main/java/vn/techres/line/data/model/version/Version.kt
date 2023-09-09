package vn.techres.line.data.model.version

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Version : Serializable{
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["version"])
    var version: String? = ""

    @JsonField(name = ["message"])
    var message: String? = ""

    @JsonField(name = ["download_link"])
    var download_link: String? = ""

    @JsonField(name = ["is_require_update"])
    var is_require_update: Int? = 0
}