package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.request.BaseRequest
import vn.techres.line.data.model.request.CreateUserInformationNodeRequest

@JsonObject
class UserNodejsParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = CreateUserInformationNodeRequest()
}