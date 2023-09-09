package vn.techres.line.data.model.game.drink.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CheckPlayTimesRequest : Serializable {

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int = 0

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String = ""

    @JsonField(name = ["branch_id"])
    var branch_id: Int = 0

    @JsonField(name = ["branch_name"])
    var branch_name: String = ""
}