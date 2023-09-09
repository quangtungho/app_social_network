package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.notification.NotifyChatRequest
import vn.techres.line.data.model.request.BaseRequest

@JsonObject
class NotifyChatParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = NotifyChatRequest()
}