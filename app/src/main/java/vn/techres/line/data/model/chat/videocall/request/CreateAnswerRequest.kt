package vn.techres.line.data.model.chat.videocall.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CreateAnswerRequest : Serializable {

//    @JsonField(name = ["offer"])
//    var answer : SessionDescription? = null

    @JsonField(name = ["group_id"])
    var group_id : String? = ""

    @JsonField(name = ["member_id"])
    var member_id : Int? = 0

    @JsonField(name = ["type"])
    var type : String? = ""

    @JsonField(name = ["description"])
    var description : String? = ""
}