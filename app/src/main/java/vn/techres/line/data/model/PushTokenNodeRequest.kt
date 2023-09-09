package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class PushTokenNodeRequest : Serializable {
    @JsonField(name = ["device_uid"])
    var device_uid: String? = ""

    @JsonField(name = ["push_token"])
    var push_token: String? = ""

    @JsonField(name = ["os_name"])
    var os_name: String? = ""

    @JsonField(name = ["customer_id"])
    var customer_id: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name: String? = ""
}