package vn.techres.line.data.model.chat

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
class Sender() : Serializable {

    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: Avatar? = null

    @TypeConverter
    fun fromSenderJsJson(sender: Sender) : String{
        return Gson().toJson(sender)
    }
    @TypeConverter
    fun toSenderJsJson(json : String) : Sender {
        return Gson().fromJson(json, object :
            TypeToken<Sender>() {}.type
        )
    }
    constructor(member_id: Int?, full_name: String?, avatar: Avatar?): this(){
        this.member_id = member_id
        this.full_name = full_name
        this.avatar = avatar
    }
}