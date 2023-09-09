package vn.techres.line.data.model.game

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ChatGame : Serializable {
    @JsonField(name=["user_id"])
    var user_id : Int? = null

    @JsonField(name=["full_name"])
    var full_name : String? = null

    @JsonField(name=["avatar"])
    var avatar : String? = null

    @JsonField(name=["status"])
    var status : Int? = null

    @JsonField(name=["message"])
    var message : String? = null

    @JsonField(name=["message_type"])
    var message_type : String? = null

    @JsonField(name=["restaurant_id"])
    var restaurant_id : Int? = null

    @JsonField(name=["branch_id"])
    var branch_id : Int? = null

    @JsonField(name=["room_id"])
    var room_id : Int? = null
}