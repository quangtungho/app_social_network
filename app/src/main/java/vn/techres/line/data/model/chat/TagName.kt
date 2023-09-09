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
class TagName : Serializable {
    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["key_tag_name"])
    var key_tag_name: String? = ""

    @TypeConverter
    fun fromTagNameJson(list: ArrayList<TagName>?) : String{
        return  if(list != null){
            Gson().toJson(list)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toTagNameJson(json : String): ArrayList<TagName>{
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<ArrayList<TagName>>() {}.type
            )
        }else{
            ArrayList()
        }
    }
}