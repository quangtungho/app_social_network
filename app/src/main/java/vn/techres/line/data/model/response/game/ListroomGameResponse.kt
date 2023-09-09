package vn.techres.line.data.model.response.game

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.game.RoomGame
import vn.techres.line.data.model.response.BaseResponse

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ListroomGameResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<RoomGame>()
}