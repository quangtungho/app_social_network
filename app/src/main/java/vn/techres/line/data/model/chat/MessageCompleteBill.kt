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
class MessageCompleteBill : Serializable {
    @JsonField(name = ["order_id"])
    var order_id = 0
    @JsonField(name = ["message"])
    var message = ""
    @JsonField(name = ["payment_date"])
    var payment_date = ""
    @JsonField(name = ["address"])
    var address = ""
    @JsonField(name = ["restaurant_phone"])
    var restaurant_phone = ""
    @JsonField(name = ["total_amount"])

    var branch_name = ""
    @JsonField(name = ["branch_name"])

    var total_amount = 0
    @TypeConverter
    fun fromPinnedJson(messageCompleteBill: MessageCompleteBill?) : String{
        return if(messageCompleteBill != null){
            Gson().toJson(messageCompleteBill)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toPinnedJson(json : String) : MessageCompleteBill {
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<MessageCompleteBill>() {}.type
            )
        }else{
            MessageCompleteBill()
        }
    }
}