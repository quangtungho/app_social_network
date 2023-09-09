package vn.techres.line.data.local.contact.device

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.helper.StringUtils

class AvatarDeviceDBConvert {
    @TypeConverter
    fun fromAvatarDeviceJson(uri: Uri?) : String{
        return if(uri != null){
            Gson().toJson(uri)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toAvatarDeviceJson(json : String) : Uri? {
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<Uri>() {}.type
            )
        }else{
            null
        }
    }
}