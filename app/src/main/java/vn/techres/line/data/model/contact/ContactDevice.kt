package vn.techres.line.data.model.contact

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ContactDevice() : Serializable{
    @JsonField(name = ["_id"])
    var _id: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["phone"])
    var phone: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["isCheck"])
    var isCheck: Boolean = false

    constructor(_id: Int?, full_name: String?, phone: String?, avatar: String?, isCheck: Boolean) : this(){
        this._id = _id
        this.full_name = full_name
        this.phone = phone
        this.avatar = avatar
        this.isCheck = isCheck
    }
}