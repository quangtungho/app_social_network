package vn.techres.line.data.model.chat

import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Members() : Serializable{
    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["number"])
    var number : Int? = 0

    @JsonField(name = ["is_notification"])
    var is_notification : Int? = 0

    @JsonField(name = ["permissions"])
    var permissions : String? = ""

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: Avatar? = null

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["normalize_name"])
    var normalize_name: String? = ""

    @JsonField(name = ["prefix"])
    var prefix: String? = ""

    @JsonField(name = ["status_with_member"])
    var status_with_member : Int? = 0

    @JsonField(name = ["tag_name"])
    var tag_name : String? = ""

    @JsonField(name = ["group_id"])
    var group_id  : String? = ""

    constructor(member_id: Int?, full_name: String?, avatar: Avatar?, permissions : String?, status: Int?) : this(){
        this.member_id = member_id
        this.full_name = full_name
        this.avatar = avatar
        this.permissions = permissions
        this.status = status
    }
    @TypeConverter
    fun fromMembersJson(members: Members) : String{
        return Gson().toJson(members)
    }
    @TypeConverter
    fun toMemberJson(json : String) : Members{
        return Gson().fromJson(json, object :
            TypeToken<Members>() {}.type
        )
    }
}
