package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class DetailGroup : Serializable {
    @JsonField(name = ["background"])
    var background : String? = ""

    @JsonField(name = ["group_id"])
    var group_id : String? = ""

    @JsonField(name = ["members"])
    var members = ArrayList<Members>()

    @JsonField(name = ["group_name"])
    var group_name : String? = ""

    @JsonField(name = ["avatar"])
    var avatar : String? = ""

    @JsonField(name = ["list_image"])
    var list_image = ArrayList<ImageVideo>()

    @JsonField(name = ["total_image"])
    var total_image : Int? = 0

    @JsonField(name = ["is_notification"])
    var is_notification : Int? = 0
    @JsonField(name = ["activities_status"])
    var activities_status : String? = ""
}