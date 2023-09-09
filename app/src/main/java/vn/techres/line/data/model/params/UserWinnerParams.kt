package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.game.luckywheel.request.UserWinnerRequest
import vn.techres.line.data.model.request.BaseRequest

class UserWinnerParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = UserWinnerRequest()
}