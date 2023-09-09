package vn.techres.line.data.local.message

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.helper.StringUtils

class ListMessageDBConvert {
    @TypeConverter
    fun fromListMessageDBConvertJson(list: ArrayList<MessagesByGroup>?) : String{
        return if(list != null){
            Gson().toJson(list)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toListMessageDBConvertJson(json : String) : ArrayList<MessagesByGroup> {
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<ArrayList<MessagesByGroup>>() {}.type
            )
        }else{
            ArrayList()
        }
    }
}