package vn.techres.line.data.model.contact

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.local.contact.device.PhoneDBConvert
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(tableName = "contactTable")
class ContactData() : Serializable {

    @PrimaryKey(autoGenerate = true)
    var uid : Int? = null

    @ColumnInfo(name = "contact_id")
    @JsonField(name = ["contact_id"])
    var contact_id: Long? = 0L

    @ColumnInfo(name = "full_name")
    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @ColumnInfo(name = "phone")
    @JsonField(name = ["phone"])
    @TypeConverters(PhoneDBConvert::class)
    var phone: List<String>? = listOf()

    @ColumnInfo(name = "avatar")
    @JsonField(name = ["avatar"])
    var avatar: String? = null

    constructor(contact_id: Long?,  full_name: String?, phone: List<String>, avatar: String) : this(){
        this.contact_id = contact_id
        this.full_name = full_name
        this.phone = phone
        this.avatar = avatar
    }
}