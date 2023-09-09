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
class MessageViewer() : Serializable {
    @JsonField(name = ["avatar"])
    var avatar: Avatar? = null

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

//    @JsonField(name = ["group_id"])
//    var group_id: String? = ""

    constructor(avatar: Avatar?, full_name: String?, member_id: Int?) : this() {
        this.avatar = avatar
        this.full_name = full_name
        this.member_id = member_id
    }

    @TypeConverter
    fun fromMessageViewerJson(list: ArrayList<MessageViewer>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toMessageViewerJson(json: String): ArrayList<MessageViewer> {
        return Gson().fromJson(
            json, object :
                TypeToken<ArrayList<MessageViewer>>() {}.type
        )
    }
}