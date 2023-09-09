package vn.techres.line.data.model.game.drink

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Beverage : Serializable {
    @JsonField(name = ["_id"])
    var _id: String = ""

    @JsonField(name = ["status"])
    var status: Int = 0

    @JsonField(name = ["drink_name"])
    var drink_name: String = ""

    @JsonField(name = ["total_vote"])
    var total_vote: Int = 0
}