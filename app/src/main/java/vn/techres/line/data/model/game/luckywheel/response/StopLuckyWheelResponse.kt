package vn.techres.line.data.model.game.luckywheel.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class StopLuckyWheelResponse : Serializable {
    @JsonField(name = ["user_id"])
    var user_id: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name: String? =""

    @JsonField(name = ["phone"])
    var phone: String? =""

    @JsonField(name = ["gift_avatar"])
    var gift_avatar: String? =""

    @JsonField(name = ["gift_name"])
    var gift_name: String? =""

    @JsonField(name = ["gender"])
    var gender: Int? = 0

    @JsonField(name = ["money"])
    var money: Int? = 0
}