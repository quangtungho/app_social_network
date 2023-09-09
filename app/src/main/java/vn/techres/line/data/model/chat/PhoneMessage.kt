package vn.techres.line.data.model.chat

import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.data.model.utils.Avatar
import vn.techres.line.helper.StringUtils
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class PhoneMessage : Serializable {
    @JsonField(name = ["avatar"])
    var avatar: Avatar? = null

    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["phone"])
    var phone: String? = ""

    @TypeConverter
    fun fromPhoneMessageJson(phoneMessage: PhoneMessage?): String {
        return if (phoneMessage != null) {
            Gson().toJson(phoneMessage)
        } else {
            ""
        }
    }

    @TypeConverter
    fun toPhoneMessageJson(json: String): PhoneMessage {
        return if (!StringUtils.isNullOrEmpty(json)) {
            Gson().fromJson(
                json, object :
                    TypeToken<PhoneMessage>() {}.type
            )
        } else {
            PhoneMessage()
        }
    }
}