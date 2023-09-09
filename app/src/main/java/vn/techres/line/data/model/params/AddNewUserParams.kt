package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.chat.request.AddNewUserRequest
import vn.techres.line.data.model.request.BaseRequest

@JsonObject
class AddNewUserParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = AddNewUserRequest()
}