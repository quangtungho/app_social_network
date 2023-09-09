package vn.techres.line.data.model.game.luckywheel

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.SenderGameLuckyWheel
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class MessageGameLuckyWheel : Serializable{
    @JsonField(name=["sender"])
    var sender : SenderGameLuckyWheel? = null

    @JsonField(name=["message"])
    var message : String? = ""

    @JsonField(name=["random_number_id"])
    var random_number_id : String? = ""

    @JsonField(name=["message_type"])
    var message_type : String? = ""

    @JsonField(name=["branch_id"])
    var branch_id : Int? = 0

    @JsonField(name=["restaurant_id"])
    var restaurant_id : Int? = 0

    @JsonField(name=["room_id"])
    var room_id : String? = ""

    @JsonField(name=["created_at"])
    var created_at : String? = ""
}