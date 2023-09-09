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
class MessageVideoCall : Serializable {
    @JsonField(name = ["sender"])
    var sender: Sender? = null

    @JsonField(name = ["call_status"])
    var call_status: String? = ""

    @JsonField(name = ["call_time"])
    var call_time: String? = ""

    @TypeConverter
    fun fromMessageVideoCallJson(messageVideoCall: MessageVideoCall?) : String{
        return if(messageVideoCall != null){
            Gson().toJson(messageVideoCall)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toMessageVideoCallJson(json : String) : MessageVideoCall {
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<MessageVideoCall>() {}.type
            )
        }else{
            MessageVideoCall()
        }
    }
}