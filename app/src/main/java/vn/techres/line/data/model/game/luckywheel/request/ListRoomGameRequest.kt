package vn.techres.line.data.model.game.luckywheel.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ListRoomGameRequest : Serializable {
    @JsonField(name=["id_article_game"])
    var id_article_game : String? = ""

    @JsonField(name=["current_type"])
    var current_type : String? = ""

    @JsonField(name=["restaurant_id"])
    var restaurant_id : Int? = 0
}