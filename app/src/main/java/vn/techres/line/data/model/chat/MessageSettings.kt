package vn.techres.line.data.model.chat

import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.helper.StringUtils
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 29/04/2022
 */

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class MessageSettings : Serializable {
    @JsonField(name = ["message"])
    var message: String? = ""

    @JsonField(name = ["image_getting"])
    var image_getting: String? = ""

    @TypeConverter
    fun fromPinnedJson(messageSettings: MessageSettings?) : String{
        return if(messageSettings != null){
            Gson().toJson(messageSettings)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toPinnedJson(json : String) : MessageSettings {
        return if (!StringUtils.isNullOrEmpty(json)) {
            Gson().fromJson(
                json, object :
                    TypeToken<MessageSettings>() {}.type
            )
        } else {
            MessageSettings()
        }
    }
}