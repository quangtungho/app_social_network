package vn.techres.line.data.model.game

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.game.luckywheel.MessageGameLuckyWheel
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ListDataMessage : Serializable{
    @JsonField(name = ["list"])
    var list = ArrayList<MessageGameLuckyWheel>()

    @JsonField(name = ["totalMessage"])
    var totalMessage: Int? = 0
}