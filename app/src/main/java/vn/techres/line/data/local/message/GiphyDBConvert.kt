package vn.techres.line.data.local.message

import androidx.room.TypeConverter
import com.giphy.sdk.core.models.Images
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.core.models.Video
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.helper.StringUtils

class GiphyDBConvert {
    @TypeConverter
    fun fromReactionJson(media: Media?) : String{
        return if(media != null){
            Gson().toJson(media)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toReactionJson(json : String) : Media {
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<Media>() {}.type
            )
        }else{
            Media("", null, "", "", "", "", "", "", "", null, "",
            null, null, null, Images(), Video(), "", ""
            )
        }
    }
}