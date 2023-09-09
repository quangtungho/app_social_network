package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.request.BaseRequest
import vn.techres.line.data.model.request.UpdateAvatarRequest

class UpdateAvatarParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = UpdateAvatarRequest()
}