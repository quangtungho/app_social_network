package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class UserRequest {
    @JsonField(name = ["username"])
    var username: String? = null

    @JsonField(name = ["password"])
    var password: String? = null

    @JsonField(name = ["external_uid"])
    var external_uid: String? = null

    @JsonField(name = ["auth_type"])
    var auth_type: Int? = 1

    @JsonField(name = ["email"])
    var email: String? = null

    @JsonField(name = ["phone"])
    var phone: String? = null

    @JsonField(name = ["name"])
    var name: String? = null

    @JsonField(name = ["device_uid"])
    var device_uid: String? = null

    @JsonField(name = ["os_name"])
    var os_name: String? = null





}

