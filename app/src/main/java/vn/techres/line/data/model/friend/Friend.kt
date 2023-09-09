package vn.techres.line.data.model.friend

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.Member
import vn.techres.line.data.model.utils.Avatar
import vn.techres.line.roomdatabase.Converters
import java.io.Serializable

@JsonObject
@Entity(tableName = "tbl_friends")
class Friend : Serializable {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @JsonField(name = ["_id"])
    @ColumnInfo(name = "_id")
    var _id: String? = ""

    @JsonField(name = ["user_id"])
    @ColumnInfo(name = "user_id")
    var user_id: Int? = 0

    @JsonField(name = ["restaurant_id"])
    @ColumnInfo(name = "restaurant_id")
    var restaurant_id: Int? = 0

    @JsonField(name = ["full_name"])
    @ColumnInfo(name = "full_name")
    var full_name: String? = ""

    @JsonField(name = ["avatar"])
    @ColumnInfo(name = "avatar")
    @TypeConverters(Avatar::class)
    var avatar = Avatar()

    @JsonField(name = ["cover_urls"])
    @ColumnInfo(name = "cover_urls")
    @TypeConverters(Converters::class)
    var cover_urls = ArrayList<String>()

    @JsonField(name = ["phone"])
    @ColumnInfo(name = "phone")
    var phone: String? = ""

    @JsonField(name = ["gender"])
    @ColumnInfo(name = "gender")
    var gender: Int? = 0

    @JsonField(name = ["status"])
    @ColumnInfo(name = "status")
    var status: Int? = 0

    @JsonField(name = ["birthday"])
    @ColumnInfo(name = "birthday")
    var birthday: String? = ""

    @JsonField(name = ["created_at"])
    @ColumnInfo(name = "created_at")
    var created_at: String? = ""

    @JsonField(name = ["friend"])
    @ColumnInfo(name = "friend")
    @TypeConverters(Member::class)
    var friend = Member()

    @JsonField(name = ["contact_from"])
    @ColumnInfo(name = "contact_from")
    @TypeConverters(Member::class)
    var contact_from = Member()

    @JsonField(name = ["prefix"])
    @ColumnInfo(name = "prefix")
    var prefix: String? = ""

    @JsonField(name = ["normalize_name"])
    @ColumnInfo(name = "normalize_name")
    var normalize_name: String? = ""

    @JsonField(name = ["mutual_friend"])
    @ColumnInfo(name = "mutual_friend")
    @TypeConverters(MutualFriend::class)
    var mutual_friend = ArrayList<MutualFriend>()

    @JsonField(name = ["total_mutual_friend"])
    @ColumnInfo(name = "total_mutual_friend")
    var total_mutual_friend: Int? = 0

    @JsonField(name = ["close_friend"])
    @ColumnInfo(name = "close_friend")
    var close_friend: Int? = 0

    @JsonField(name = ["isCheck"])
    @ColumnInfo(name = "isCheck")
    var isCheck: Boolean? = false

    /**
     * Object friend type
     * friends = 0
     * My friends = 1
     * Friends online = 2
     * Friends new = 3
     * Best friends = 4
     * Friends request = 5
     * Friends suggest = 6
     */
    @JsonField(name = ["type"])
    @ColumnInfo(name = "type")
    var type: Int? = 0
}