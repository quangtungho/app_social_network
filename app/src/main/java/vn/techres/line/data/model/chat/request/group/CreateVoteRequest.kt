package vn.techres.line.data.model.chat.request.group

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.Vote
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CreateVoteRequest : Serializable {
    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["group_id"])
    var group_id: String? = ""

    @JsonField(name = ["message_type"])
    var message_type: String? = ""

    @JsonField(name = ["message_vote"])
    var message_vote : Vote? = null

    @JsonField(name = ["key_message_error"])
    var key_message_error: String? = ""
}