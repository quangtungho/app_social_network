package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class SenderGameLuckyWheel() : Serializable {
    @JsonField(name = ["user_id"])
    var user_id: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: Avatar? = null

    @JsonField(name = ["permission"])
    var permission: String? = ""

    constructor(user_id: Int, full_name: String, avatar: Avatar, permission: String) : this(){
        this.user_id = user_id
        this.full_name = full_name
        this.avatar = avatar
        this.permission = permission
    }

}