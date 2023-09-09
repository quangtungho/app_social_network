package vn.techres.line.data.model.friend

import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class MutualFriend : Serializable {
    @JsonField(name = ["_id"])
    var _id: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["friend_avatar"])
    var friend_avatar = Avatar()

    @TypeConverter
    fun fromMutualFriendJson(list: ArrayList<MutualFriend>): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toMutualFriendJson(json: String?): ArrayList<MutualFriend>? {
        return Gson().fromJson(
            json, object :
                TypeToken<ArrayList<MutualFriend>>() {}.type
        )
    }
}