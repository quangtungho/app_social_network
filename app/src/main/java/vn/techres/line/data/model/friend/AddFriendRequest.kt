package vn.techres.line.data.model.friend

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class AddFriendRequest : Serializable {

    @JsonField(name = ["user_id_sender"])
    var user_id_sender: Int? = 0

    @JsonField(name = ["avatar_sender"])
    var avatar_sender: String? = ""

    @JsonField(name = ["full_name_sender"])
    var full_name_sender: String? = ""

    @JsonField(name = ["phone_sender"])
    var phone_sender: String? = ""

    @JsonField(name = ["user_id_received"])
    var user_id_received: Int? = 0

    @JsonField(name = ["avatar_received"])
    var avatar_received: String? = ""

    @JsonField(name = ["full_name_received"])
    var full_name_received: String? = ""

    @JsonField(name = ["phone_received"])
    var phone_received: String? = ""
}