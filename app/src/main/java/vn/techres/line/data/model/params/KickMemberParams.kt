package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.chat.request.KickMemberRequest
import vn.techres.line.data.model.request.BaseRequest

@JsonObject
class KickMemberParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = KickMemberRequest()
}