package vn.techres.line.data.local.contact.device

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.helper.StringUtils

class PhoneDBConvert {
    @TypeConverter
    fun fromSPhoneJson(list: List<String>?) : String{
        return if(list != null){
            Gson().toJson(list)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toPhoneJson(json : String) : List<String> {
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<List<String>>() {}.type
            )
        }else{
            listOf()
        }
    }
}