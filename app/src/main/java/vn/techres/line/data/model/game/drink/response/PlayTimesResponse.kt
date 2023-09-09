package vn.techres.line.data.model.game.drink.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.game.drink.PlayTimes
import vn.techres.line.data.model.response.BaseResponse

class PlayTimesResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = PlayTimes()
}