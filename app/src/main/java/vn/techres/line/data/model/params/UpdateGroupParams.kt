package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.chat.request.UpdateGroupRequest
import vn.techres.line.data.model.request.BaseRequest

@JsonObject
class UpdateGroupParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = UpdateGroupRequest()
}