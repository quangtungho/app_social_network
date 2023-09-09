package vn.techres.line.data.model.account

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class InformationLogin : Serializable {
    @JsonField(name = ["device_uid"])
    var device_uid: String? = ""

    @JsonField(name = ["device_name"])
    var device_name: String? = ""

    @JsonField(name = ["ip_address"])
    var ip_address: String? = ""

    @JsonField(name = ["last_login_time"])
    var last_login_time: String? = ""
}