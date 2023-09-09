package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class RemoveGroupResponse : Serializable{
//    @JsonField(name = ["sender"])
//    var sender = Sender()

    @JsonField(name = ["receiver_id"])
    var receiver_id : String? = ""

//    @JsonField(name = ["message"])
//    var message : String? = ""
//
//    @JsonField(name = ["message_type"])
//    var message_type : String? = ""
}