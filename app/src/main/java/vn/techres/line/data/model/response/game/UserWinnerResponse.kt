package vn.techres.line.data.model.response.game

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.response.BaseResponse

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class UserWinnerResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data = ArrayList<UserWinner>()
}