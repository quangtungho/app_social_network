package vn.techres.line.data.model.game.drink

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class PlayTimes : Serializable {
    @JsonField(name = ["max_number_plays"])
    var max_number_plays: Int? = 0

    @JsonField(name = ["current_number_plays"])
    var current_number_plays: Int? = 0

}