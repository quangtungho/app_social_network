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
class Vote : Serializable {
    @JsonField(name = ["title"])
    var title: String? = ""

    @JsonField(name = ["list_option"])
    var list_option = ArrayList<OptionVote>()

    @JsonField(name = ["multi_chose"])
    var multi_chose: Int? = 0

    @JsonField(name = ["allow_add_option"])
    var allow_add_option: Int? = 0

    @JsonField(name = ["number_user_vote"])
    var number_user_vote: Int? = 0

    @JsonField(name = ["number_vote"])
    var number_vote: Int? = 0

    @JsonField(name = ["message"])
    var message: String? = ""

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["time_create"])
    var time_create: String? = ""

    @JsonField(name = ["deadline"])
    var deadline: String? = ""

    @TypeConverter
    fun fromVoteJson(vote: Vote?) : String{
        return if(vote != null){
            Gson().toJson(vote)
        }else{
            ""
        }
    }
    @TypeConverter
    fun toVoteJson(json : String) : Vote {
        return if(!StringUtils.isNullOrEmpty(json)){
            Gson().fromJson(json, object :
                TypeToken<Vote>() {}.type
            )
        }else{
            Vote()
        }
    }
}