package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.game.drink.DrinkGame

@JsonObject
class DrinkGameResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = DrinkGame()
}