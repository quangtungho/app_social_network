package vn.techres.line.data.model.chat

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.helper.techresenum.TechResEnumChat
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(tableName = "groupTable")
class Group() : Serializable{

    @PrimaryKey(autoGenerate = true)
    var uid : Int? = null

    @ColumnInfo(name = "_id")
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @ColumnInfo(name = "admin_id")
    @JsonField(name = ["admin_id"])
    var admin_id: Int? = 0

    @ColumnInfo(name = "name")
    @JsonField(name = ["name"])
    var name: String? = ""

    @ColumnInfo(name = "avatar")
    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @ColumnInfo(name = "background")
    @JsonField(name = ["background"])
    var background: String? = ""

    @ColumnInfo(name = "status_last_message")
    @JsonField(name = ["status_last_message"])
    var status_last_message: Int? = 0

    @ColumnInfo(name = "last_message")
    @JsonField(name = ["last_message"])
    var last_message: String? = ""

    @ColumnInfo(name = "user_last_message_id")
    @JsonField(name = ["user_last_message_id"])
    var user_last_message_id: Int? = 0

    @ColumnInfo(name = "user_name_last_message")
    @JsonField(name = ["user_name_last_message"])
    var user_name_last_message: String? = ""

    @ColumnInfo(name = "last_message_type")
    @JsonField(name = ["last_message_type"])
    var last_message_type: String? = ""

    @ColumnInfo(name = "conversation_type")
    @JsonField(name = ["conversation_type"])
    var conversation_type: String? = ""

    @ColumnInfo(name = "list_tag_name")
    @JsonField(name = ["list_tag_name"])
    @TypeConverters(TagName::class)
    var list_tag_name : ArrayList<TagName>? = null

    @ColumnInfo(name = "prefix")
    @JsonField(name = ["prefix"])
    var prefix: String? = ""

    @ColumnInfo(name = "normalize_name")
    @JsonField(name = ["normalize_name"])
    var normalize_name: String? = ""

    @ColumnInfo(name = "created_last_message")
    @JsonField(name = ["created_last_message"])
    var created_last_message: String? = ""

    @ColumnInfo(name = "call_status")
    @JsonField(name = ["call_status"])
    var call_status: String? = ""

    @ColumnInfo(name = "is_call_phone")
    @JsonField(name = ["is_call_phone"])
    var call_phone: Int? = 0

    @ColumnInfo(name = "is_call_video")
    @JsonField(name = ["is_call_video"])
    var call_video: Int? = 0

    @JsonField(name = ["isCheck"])
    var isCheck: Boolean? = false

    @ColumnInfo(name = "member")
    @JsonField(name = ["member"])
    @TypeConverters(Members::class)
    var member = Members()

    @ColumnInfo(name = "number_message_not_seen")
    @JsonField(name = ["number_message_not_seen"])
    var number_message_not_seen : Int? = 0

    @ColumnInfo(name = "page")
    @JsonField(name = ["page"])
    var page: Int? = 0

    @ColumnInfo(name = "is_stranger")
    @JsonField(name = ["is_stranger"])
    var stranger: Int? = 0

    @ColumnInfo(name = "is_created_by_contact_phone")
    @JsonField(name = ["is_created_by_contact_phone"])
    var created_by_contact_phone : Int? = 0

    @ColumnInfo(name = "total_message")
    @JsonField(name = ["total_message"])
    var total_message : Int? = 0

    @ColumnInfo(name = "created_at_time")
    @JsonField(name = ["created_at_time"])
    var created_at_time: String? = ""

    @ColumnInfo(name = "user_id")
    @JsonField(name = ["user_id"])
    var user_id: Int = 0
    @ColumnInfo(name = "activities_status")
    @JsonField(name = ["activities_status"])
    var activities_status: String = ""

    constructor(_id: String, name: String, avatar: String, background : String) : this(){
        this._id = _id
        this.name = name
        this.avatar = avatar
        this.background = background
    }

    constructor(_id: String, conversationType: String) : this(){
        this._id = _id
        this.conversation_type = conversationType
    }

    override fun equals(other: Any?): Boolean {
        if(javaClass != other?.javaClass){
            return false
        }
        other as Group
        if(_id != other._id){
            return false
        }
        if(name != other.name){
            return false
        }
        if(avatar != other.avatar){
            return false
        }
        if(background != other.background){
            return false
        }
        if(status_last_message != other.status_last_message){
            return false
        }
        if(last_message != other.last_message){
            return false
        }
        if(user_name_last_message != other.user_name_last_message){
            return false
        }
        if(last_message_type != other.last_message_type){
            return false
        }
        if(list_tag_name != other.list_tag_name){
            return false
        }
        if(created_last_message != other.created_last_message){
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = _id?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (admin_id ?: 0)
        result = 31 * result + (avatar?.hashCode() ?: 0)
        result = 31 * result + (background?.hashCode() ?: 0)
        result = 31 * result + (status_last_message ?: 0)
        result = 31 * result + (last_message?.hashCode() ?: 0)
        result = 31 * result + (user_name_last_message?.hashCode() ?: 0)
        result = 31 * result + (last_message_type?.hashCode() ?: 0)
        result = 31 * result + (conversation_type?.hashCode() ?: 0)
        result = 31 * result + (list_tag_name?.hashCode() ?: 0)
        result = 31 * result + (prefix?.hashCode() ?: 0)
        result = 31 * result + (normalize_name?.hashCode() ?: 0)
        result = 31 * result + (created_last_message?.hashCode() ?: 0)
        result = 31 * result + (isCheck?.hashCode() ?: 0)
        result = 31 * result + member.hashCode()
        return result
    }
    fun copy() = Group()

}

object GroupDiffUtilCallback : DiffUtil.ItemCallback<Group>() {
    override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean =
        oldItem._id == newItem._id

    override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Group, newItem: Group): Any {
        val bundle  = Bundle()
        bundle.putString(TechResEnumChat.GROUP.toString(), TechResEnumChat.GROUP.toString())
        return bundle
    }
}

