package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class CreateUserInformationNodeRequest : Serializable {

    @JsonField(name = ["os_name"])
    var os_name: String? = ""

    @JsonField(name = ["user_id"])
    var user_id: Int? = 0

    @JsonField(name = ["device_name"])
    var device_name: String? = ""

    @JsonField(name = ["device_uid"])
    var device_uid: String? = ""

    @JsonField(name = ["ip_address"])
    var ip_address: String? = ""

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0
}