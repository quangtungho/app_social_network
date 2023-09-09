package vn.techres.line.data.model.response.game

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.game.QRCodeGame
import vn.techres.line.data.model.response.BaseResponse

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class JoinGameResponse : BaseResponse() {

    @JsonField(name = ["data"])
    var data: QRCodeGame? = null

}