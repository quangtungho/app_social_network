package vn.techres.line.data.model.game.luckywheel.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class TotalCustomerJoinRoomResponse : Serializable {
    @JsonField(name = ["new_user"])
    var new_user: String? = ""

    @JsonField(name = ["total_user"])
    var total_user: Int? = 0
}