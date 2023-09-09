package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Background : Serializable {
    @JsonField(name = ["name"])
    var name : String? = ""

    @JsonField(name = ["link_original"])
    var link_original : String? = ""

    @JsonField(name = ["_id"])
    var _id : String? = ""

    @JsonField(name = ["is_Check"])
    var is_Check : Boolean? = false
}