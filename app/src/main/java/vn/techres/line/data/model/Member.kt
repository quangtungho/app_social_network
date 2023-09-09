package vn.techres.line.data.model

import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.Gson
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.utils.User
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Member : Serializable {
    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["total_contact_to"])
    var total_contact_to: Int? = 0

    @JsonField(name = ["total_contact_from"])
    var total_contact_from: Int? = 0

    @JsonField(name = ["createdAt"])
    var createdAt: String? = ""

    @JsonField(name = ["updatedAt"])
    var updatedAt: String? = ""

    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["user_id"])
    var user_id: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["username"])
    var username: String? = ""

    @JsonField(name = ["phone"])
    var phone: String? = ""

    @JsonField(name = ["nameApp"])
    var nameApp: String? = ""

    @JsonField(name = ["prefix"])
    var prefix: String? = ""

    @JsonField(name = ["normalize_name"])
    var normalize_name: String? = ""

    @JsonField(name = ["list_frienlds"])
    var list_frienlds = ArrayList<User>()

    @JsonField(name = ["contact_to"])
    var contact_to = ArrayList<Friend>()

    @JsonField(name = ["contact_from"])
    var contact_from = ArrayList<User>()

    @JsonField(name = ["__v"])
    var __v: Int? = 0

    @TypeConverter
    fun fromMemberJson(stat: Member?): String? {
        return Gson().toJson(stat)
    }

    @TypeConverter
    fun toMemberJson(json: String?): Member? {
        return Gson().fromJson(
            json,
            Member::class.java
        )
    }
}