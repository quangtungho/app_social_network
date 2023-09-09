package vn.techres.line.data.model.game.drink.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.game.drink.Beverage
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class UpdateChooseTimesRequest : Serializable {
    @JsonField(name = ["beverage_lists"])
    var beverage_lists = ArrayList<Beverage>()

    @JsonField(name = ["beverage_id"])
    var beverage_id : String? = ""
}