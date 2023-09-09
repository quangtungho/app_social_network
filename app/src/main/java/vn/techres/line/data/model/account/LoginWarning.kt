package vn.techres.line.data.model.account

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class LoginWarning : Serializable{
    @JsonField(name = ["_id"])
    var _id : String? = ""

    @JsonField(name = ["username"])
    var username : String? = ""

    @JsonField(name = ["user_id"])
    var user_id : Int? = 0

    @JsonField(name = ["full_name"])
    var full_name : String? = ""

    @JsonField(name = ["os_name"])
    var os_name : String? = ""

    @JsonField(name = ["phone"])
    var phone : String? = ""

    @JsonField(name = ["node_access_token"])
    var node_access_token : String? = ""

    @JsonField(name = ["node_refesh_token"])
    var node_refesh_token : String? = ""

    @JsonField(name = ["device_uid"])
    var device_uid : String? = ""

    @JsonField(name = ["created_at"])
    var created_at : String? = ""

    @JsonField(name = ["updated_at"])
    var updated_at : String? = ""

    @JsonField(name = ["device_name"])
    var device_name : String? = ""

    @JsonField(name = ["ip_address"])
    var ip_address : String? = ""

    @JsonField(name = ["last_login_time"])
    var last_login_time : String? = ""

    @JsonField(name = ["avatar"])
    var avatar : Avatar? = null
}