package vn.techres.line.data.model.chat

import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.helper.StringUtils
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Sticker : Serializable {
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["link_original"])
    var link_original: String? = ""

    @JsonField(name = ["id_category"])
    var id_category: String? = ""

    @JsonField(name = ["key_search"])
    var key_search: ArrayList<String>? = null

    @TypeConverter
    fun fromStickerJson(list: ArrayList<Sticker>?) : String{
        return if(list != null){
            Gson().toJson(list)
        }else{
            ""
        }
    }

    @TypeConverter
    fun toStickerJson(json : String) : ArrayList<Sticker>{
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<ArrayList<Sticker>>() {}.type
            )
        }else{
            ArrayList()
        }
    }
}