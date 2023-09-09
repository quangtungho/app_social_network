package vn.techres.line.data.model.chat.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ReactionMessageRequest : Serializable {

    @JsonField(name = ["group_id"])
    var group_id : String? = ""

    @JsonField(name = ["random_key"])
    var random_key : String? = ""

    @JsonField(name = ["member_id"])
    var member_id : Int? = 0

    @JsonField(name = ["last_reactions"])
    var last_reactions : Int? = 0

    @JsonField(name = ["key_message_error"])
    var key_message_error : String? = ""

    @JsonField(name = ["last_reactions_id"])
    var last_reactions_id : Int? = 0
}