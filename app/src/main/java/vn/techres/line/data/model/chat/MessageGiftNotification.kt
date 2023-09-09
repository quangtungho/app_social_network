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
 * @Date: 26/04/2022
 */

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class MessageGiftNotification : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["title"])
    var title: String? = ""

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["banner"])
    var banner: String? = ""

    @JsonField(name = ["expire_at"])
    var expire_at: String? = ""

    @JsonField(name = ["address"])
    var address: String? = ""



    @TypeConverter
    fun fromPinnedJson(messageGiftNotification: MessageGiftNotification?) : String{
        return if(messageGiftNotification != null){
            Gson().toJson(messageGiftNotification)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toPinnedJson(json : String) : MessageGiftNotification {
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<MessageGiftNotification>() {}.type
            )
        }else{
            MessageGiftNotification()
        }
    }
}