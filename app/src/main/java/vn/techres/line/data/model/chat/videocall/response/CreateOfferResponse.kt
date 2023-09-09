package vn.techres.line.data.model.chat.videocall.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.Receiver
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CreateOfferResponse : Serializable {
    @JsonField(name = ["type"])
    var type : String? = ""

    @JsonField(name = ["description"])
    var description : String? = ""

    @JsonField(name = ["offer"])
    var offer : String? = ""

    @JsonField(name = ["receiver"])
    var receiver = Receiver()
}