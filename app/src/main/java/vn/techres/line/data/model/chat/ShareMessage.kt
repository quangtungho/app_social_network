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
class ShareMessage : Serializable{
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["message_type"])
    var message_type: String? = ""

    @JsonField(name = ["random_key"])
    var random_key: String? = ""

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["today"])
    var today: Int? = 0

    @JsonField(name = ["sender"])
    var sender : Sender? = null

    @JsonField(name = ["files"])
    var files = ArrayList<FileNodeJs>()

    @JsonField(name = ["message"])
    var message: String? = ""

    @JsonField(name = ["message_reply"])
    var message_reply : Reply? = null

    @JsonField(name = ["message_phone"])
    var message_phone : PhoneMessage? = null

    @JsonField(name = ["message_link"])
    var message_link : LinkMessage? = null

    @JsonField(name = ["list_tag_name"])
    var list_tag_name = ArrayList<TagName>()

    @TypeConverter
    fun fromShareMessageJson(shareMessage: ShareMessage?) : String{
        return if(shareMessage != null){
            Gson().toJson(shareMessage)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toShareMessageJson(json : String) : ShareMessage {
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<ShareMessage>() {}.type
            )
        }else{
            ShareMessage()
        }
    }
}