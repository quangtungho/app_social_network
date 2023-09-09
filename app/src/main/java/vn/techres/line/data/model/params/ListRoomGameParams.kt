package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.game.luckywheel.request.ListRoomGameRequest
import vn.techres.line.data.model.request.BaseRequest

class ListRoomGameParams : BaseRequest(){
    @JsonField(name = ["params"])
    var params = ListRoomGameRequest()
}