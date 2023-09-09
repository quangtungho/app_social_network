package vn.techres.line.data.model.game.drink

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class DrinkTable : Serializable{
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["nameTab"])
    var nameTab: String? = ""
}