package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.notification.NotifyAppRequest
import vn.techres.line.data.model.request.BaseRequest

@JsonObject
class NotifyAppParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = NotifyAppRequest()
}