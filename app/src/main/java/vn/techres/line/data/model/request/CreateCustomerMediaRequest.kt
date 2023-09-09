package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class CreateCustomerMediaRequest {

    @JsonField(name = ["customer_media_album_id"])
    var customer_media_album_id: Int? = 0

    @JsonField(name = ["media_type"])
    var media_type: Int? = 0

    @JsonField(name = ["url"])
    var url: String? = ""
}