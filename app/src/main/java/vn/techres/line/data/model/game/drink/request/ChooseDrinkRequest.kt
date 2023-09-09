package vn.techres.line.data.model.game.drink.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ChooseDrinkRequest : Serializable {
    @JsonField(name = ["beverage_id"])
    var beverage_id: String = ""

    @JsonField(name = ["drink_code"])
    var drink_code : Int = 0
}