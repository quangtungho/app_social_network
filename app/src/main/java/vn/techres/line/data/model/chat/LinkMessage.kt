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
class LinkMessage : Serializable {
    @JsonField(name = ["author"])
    var author : String? = ""

    @JsonField(name = ["media_thumb"])
    var media_thumb : String? = ""

    @JsonField(name = ["cannonical_url"])
    var cannonical_url : String? = ""

    @JsonField(name = ["title"])
    var title : String? = ""

    @JsonField(name = ["url"])
    var url : String? = ""

    @JsonField(name = ["favicon"])
    var favicon : String? = ""

    @JsonField(name = ["description"])
    var description : String? = ""

    @TypeConverter
    fun fromLinkMessageJson(linkMessage: LinkMessage?) : String{
        return if(linkMessage != null){
            Gson().toJson(linkMessage)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toLinkMessageJson(json : String) : LinkMessage {
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<LinkMessage>() {}.type
            )
        }else{
            LinkMessage()
        }
    }
}