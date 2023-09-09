package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.request.BaseRequest
import vn.techres.line.data.model.request.CreateUserInformationNodeRequest

@JsonObject
class CreateUserInformationNodeParams : BaseRequest() {
    var params = CreateUserInformationNodeRequest()
}
