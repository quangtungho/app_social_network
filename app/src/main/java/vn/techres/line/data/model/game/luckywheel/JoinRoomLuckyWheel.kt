package vn.techres.line.data.model.game.luckywheel

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class JoinRoomLuckyWheel : Serializable {
    @JsonField(name = ["user_id"])
    var user_id: Int? = 0

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["room_id"])
    var room_id: String? = ""

    @JsonField(name = ["permission"])
    var permission: String? = ""

    @JsonField(name = ["id_article_game"])
    var id_article_game: String? = ""
}