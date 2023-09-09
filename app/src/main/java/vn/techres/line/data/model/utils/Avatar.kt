package vn.techres.line.data.model.utils

import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.Gson
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Avatar() : Serializable {
    @JsonField(name = ["original"])
    var original : String? = ""

    @JsonField(name = ["medium"])
    var medium : String? = ""

    @JsonField(name = ["thumb"])
    var thumb : String? = ""

    constructor(original: String?, medium: String?, thumb: String?) : this() {
        this.original = original
        this.medium = medium
        this.thumb = thumb
    }

    @TypeConverter
    fun fromAvatarJson(stat: Avatar?): String? {
        return Gson().toJson(stat)
    }

    @TypeConverter
    fun toAvatarJson(json: String?): Avatar? {
        return Gson().fromJson(
            json,
            Avatar::class.java
        )
    }
}