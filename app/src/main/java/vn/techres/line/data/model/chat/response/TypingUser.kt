package vn.techres.line.data.model.chat.response

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
class TypingUser : Serializable {
    @JsonField(name = ["group_id"])
    var group_id : String? = ""

    @JsonField(name = ["full_name"])
    var full_name : String? = ""

    @JsonField(name = ["avatar"])
    var avatar : Avatar? = null

    @JsonField(name = ["random_key"])
    var random_key : String? = ""

    @JsonField(name = ["member_id"])
    var member_id : Int? = 0

    @TypeConverter
    fun fromTypingUserJson(typingUser: TypingUser) : String{
        return Gson().toJson(typingUser)
    }
    @TypeConverter
    fun toTypingUserJson(json : String) : TypingUser {
        return Gson().fromJson(json, object :
            TypeToken<TypingUser>() {}.type
        )
    }
}