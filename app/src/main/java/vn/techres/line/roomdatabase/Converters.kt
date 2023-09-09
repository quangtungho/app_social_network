package vn.techres.line.roomdatabase

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import vn.techres.line.helper.StringUtils

class Converters {
    @TypeConverter
    fun fromJson(list: ArrayList<String>?) : String{
        return  if(list != null){
            Gson().toJson(list)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toJson(json : String): ArrayList<String>{
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<ArrayList<String>>() {}.type
            )
        }else{
            ArrayList()
        }
    }
}