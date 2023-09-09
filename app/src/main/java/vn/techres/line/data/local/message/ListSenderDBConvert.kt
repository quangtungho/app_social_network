package vn.techres.line.data.local.message

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.data.model.chat.Sender

class ListSenderDBConvert {
    @TypeConverter
    fun fromSenderJsJson(list: ArrayList<Sender>) : String{
        return Gson().toJson(list)
    }
    @TypeConverter
    fun toSenderJsJson(json : String) : ArrayList<Sender> {
        return Gson().fromJson(json, object :
            TypeToken<ArrayList<Sender>>() {}.type
        )
    }
}