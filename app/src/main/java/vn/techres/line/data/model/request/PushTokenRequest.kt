package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class PushTokenRequest: Serializable {
    @JsonField(name = ["device_uid"])
    var device_uid: String? = ""
    @JsonField(name = ["device_name"])
    var device_name: String? = ""
    @JsonField(name = ["push_token"])
    var push_token: String? = ""
    @JsonField(name = ["os_name"])
    var os_name: String? = ""
    @JsonField(name = ["customer_id"])
    var customer_id: Int? = 0
}