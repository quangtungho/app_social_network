package vn.techres.line.data.model.game.luckywheel.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class MoneyResponse : Serializable{
    @JsonField(name = ["money"])
    var money: Int? = 0
}