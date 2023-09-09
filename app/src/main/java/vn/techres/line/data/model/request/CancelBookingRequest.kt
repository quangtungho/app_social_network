package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class CancelBookingRequest {
    @JsonField(name = ["id"])
    var id: Int? = 0
    @JsonField(name = ["cancel_reason"])
    var cancel_reason: String? = ""
}