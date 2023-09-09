package vn.techres.line.data.model.game.drink

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ChooseDrink : Serializable {
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["max_number_plays"])
    var max_number_plays: Int? = 0

    @JsonField(name = ["current_number_plays"])
    var current_number_plays: Int? = 0

    @JsonField(name = ["total_times"])
    var total_times: Int? = 0

}