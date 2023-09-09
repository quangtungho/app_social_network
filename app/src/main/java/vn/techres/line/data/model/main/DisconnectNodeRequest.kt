package vn.techres.line.data.model.main

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class DisconnectNodeRequest : Serializable {
//    @JsonField(name = ["full_name"])
//    var full_name : String? = ""

//    @JsonField(name = ["restaurant_id"])
//    var restaurant_id : Int? = 0

    @JsonField(name = ["member_id"])
    var member_id : Int? = 0

//    @JsonField(name = ["avatar"])
//    var avatar : String? = ""
    @JsonField(name = ["os_name"])
    var os_name: String? = "android"
}