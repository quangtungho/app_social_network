package vn.techres.line.data.model.chat

import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class FileNodeJs : Serializable {
    @JsonField(name = ["name_file"])
    var name_file: String? = ""

    @JsonField(name = ["link_original"])
    var link_original: String? = ""

    @JsonField(name = ["link_thumb"])
    var link_thumb: String? = ""

    @JsonField(name = ["link_medium"])
    var link_medium: String? = ""

    @JsonField(name = ["type"])
    var type: String? = ""

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["height"])
    var height: Int? = 0

    @JsonField(name = ["width"])
    var width: Int? = 0

    @JsonField(name = ["size"])
    var size: Int? = 0

    @JsonField(name = ["time"])
    var time: Int? = 0

    @JsonField(name = ["process"])
    var process: Int = 0
    @JsonField(name = ["ratio"])
    var ratio: Float = 0f
    @JsonField(name = ["random_key"])
    var random_key: String = ""

    @TypeConverter
    fun fromFileNodeJsJson(list: ArrayList<FileNodeJs>) : String{
        return Gson().toJson(list)
    }
    @TypeConverter
    fun toFileNodeJsJson(json : String) : ArrayList<FileNodeJs> {
        return Gson().fromJson(json, object :
            TypeToken<ArrayList<FileNodeJs>>() {}.type
        )
    }
}