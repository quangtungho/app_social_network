package vn.techres.line.data.model.request
import com.bluelinelabs.logansquare.annotation.JsonField


class ShopnameRequest {
    @JsonField(name = ["name"])
        var name: String? = null

        @JsonField(name = ["phone"])
        var phone: String? = null

        @JsonField(name = ["device_uid"])
        var device_uid: String? = null
}