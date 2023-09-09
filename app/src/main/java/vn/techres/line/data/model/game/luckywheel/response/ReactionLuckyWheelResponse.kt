package vn.techres.line.data.model.game.luckywheel.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ReactionLuckyWheelResponse : Serializable {
    @JsonField(name=["user_id"])
    var user_id : Int? = 0

    @JsonField(name=["type_reaction"])
    var type_reaction : Int? = 0
}