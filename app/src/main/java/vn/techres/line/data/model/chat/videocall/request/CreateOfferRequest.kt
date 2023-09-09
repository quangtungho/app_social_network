package vn.techres.line.data.model.chat.videocall.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CreateOfferRequest : Serializable {

    @JsonField(name = ["member_id"])
    var member_id : Int? = 0

    @JsonField(name = ["group_id"])
    var group_id : String? = ""

    @JsonField(name = ["type"])
    var type : String? = ""

    @JsonField(name = ["description"])
    var description : String? = ""
}