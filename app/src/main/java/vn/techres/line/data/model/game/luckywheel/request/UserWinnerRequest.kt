package vn.techres.line.data.model.game.luckywheel.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class UserWinnerRequest : Serializable {
    @JsonField(name=["branch_id"])
    var branch_id : Int? = 0

    @JsonField(name=["restaurant_id"])
    var restaurant_id : Int? = 0

    @JsonField(name=["current_type"])
    var current_type : String? = ""

    @JsonField(name=["room_id"])
    var room_id : String? = ""
}