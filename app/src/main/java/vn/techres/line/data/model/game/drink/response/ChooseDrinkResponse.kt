package vn.techres.line.data.model.game.drink.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.game.drink.ChooseDrink
import vn.techres.line.data.model.response.BaseResponse

class ChooseDrinkResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ChooseDrink()
}