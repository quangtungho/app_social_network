package vn.techres.line.data.model.game

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class RoomGame : Serializable {
    @JsonField(name=["_id"])
    var _id : String? = null

    @JsonField(name=["id_article_game"])
    var id_article_game : String? = null

    @JsonField(name=["room_id"])
    var room_id : String? = null

    @JsonField(name=["name_room"])
    var name_room : String? = null

    @JsonField(name=["full_name"])
    var full_name : String? = null

    @JsonField(name=["status"])
    var status : Int? = null

    @JsonField(name=["password"])
    var password : String? = null

    @JsonField(name=["milimum_user"])
    var milimum_user : Int? = null

    @JsonField(name=["maximum_user"])
    var maximum_user : Int? = null

    @JsonField(name=["start_playing_date"])
    var start_playing_date : String? = null

    @JsonField(name=["lucky_wheel_times"])
    var lucky_wheel_times : Int? = null

    @JsonField(name=["branch_id"])
    var branch_id : Int? = null

    @JsonField(name=["total_user_join_room"])
    var total_user_join_room : Int? = null

    @JsonField(name=["restaurant_id"])
    var restaurant_id : Int? = null

    @JsonField(name=["is_join"])
    var is_join : Int? = 0

    @JsonField(name=["prefix"])
    var prefix : String? = null

    @JsonField(name=["normalize_name"])
    var normalize_name : String? = null

    @JsonField(name=["created_at"])
    var created_at : String? = null

    @JsonField(name=["dead_time"])
    var dead_time : String? = null

    @JsonField(name=["branch_name"])
    var branch_name : String? = null


}