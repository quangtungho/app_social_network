package vn.techres.line.data.model.request
import com.bluelinelabs.logansquare.annotation.JsonField


class RegistryRequest {
    @JsonField(name=["avatar"])
    var avatar: String? = ""

    @JsonField(name=["first_name"])
    var first_name: String? = ""

    @JsonField(name=["last_name"])
    var last_name: String? = ""

    @JsonField(name=["phone"])
    var phone:String? = ""

    @JsonField(name=["device_uid"])
    var device_uid: String? = ""

    @JsonField(name=["birthday"])
    var birthday: String? = ""

    @JsonField(name=["gender"])
    var gender: Int? = 0
}