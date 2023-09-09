package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.chat.request.AuthorizeAdminRequest
import vn.techres.line.data.model.request.BaseRequest

@JsonObject
class AuthorizeAdminParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = AuthorizeAdminRequest()
}