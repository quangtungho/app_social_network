package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.notification.NotificationData

@JsonObject
class NotificationResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = NotificationData()
}
