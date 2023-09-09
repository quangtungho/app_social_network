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
class Reply : Serializable {
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["sender"])
    var sender : Sender? = null

    @JsonField(name = ["message_type"])
    var message_type: String? = ""

    @JsonField(name = ["files"])
    var files = ArrayList<FileNodeJs>()

    @JsonField(name = ["message"])
    var message: String? = ""

    @JsonField(name = ["random_key"])
    var random_key: String? = ""

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["today"])
    var today: Int? = 0

    @JsonField(name = ["message_link"])
    var message_link : LinkMessage? = null

    @JsonField(name = ["message_phone"])
    var message_phone : PhoneMessage? = null

    @JsonField(name = ["list_tag_name"])
    var list_tag_name = ArrayList<TagName>()

    @TypeConverter
    fun fromReplyJson(reply: Reply?) : String{
        return if(reply != null){
            Gson().toJson(reply)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toReplyJson(json : String) : Reply {
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<Reply>() {}.type
            )
        }else{
            Reply()
        }
    }
}