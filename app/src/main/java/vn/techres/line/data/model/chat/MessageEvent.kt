package vn.techres.line.data.model.chat

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.data.model.chat.request.Position
import vn.techres.line.data.model.utils.Avatar
import vn.techres.line.helper.StringUtils

class MessageEvent {
    var message: String = ""
    var background: String = ""
    var position: Position = Position()
    var info_receiver: InfoReceiver = InfoReceiver()
    @TypeConverter
    fun fromMessageEventJson(messageEvent: MessageEvent?) : String{
        return if(messageEvent != null){
            Gson().toJson(messageEvent)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toMessageEventJson(json : String) : MessageEvent {
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<MessageEvent>() {}.type
            )
        }else{
            MessageEvent()
        }
    }
}
class InfoReceiver {
    var avatar : Avatar = Avatar()
    var full_name : String = ""
    var member_id : Int = 0
}