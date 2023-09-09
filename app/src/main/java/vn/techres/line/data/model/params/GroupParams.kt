package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.chat.GroupRequest
import vn.techres.line.data.model.request.BaseRequest

@JsonObject
class GroupParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = GroupRequest()
}