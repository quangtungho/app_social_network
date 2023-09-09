package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.PushTokenNodeRequest
import vn.techres.line.data.model.request.BaseRequest

@JsonObject
class PushTokenNodeParams: BaseRequest() {
    @JsonField(name = ["params"])
    var params = PushTokenNodeRequest()
}