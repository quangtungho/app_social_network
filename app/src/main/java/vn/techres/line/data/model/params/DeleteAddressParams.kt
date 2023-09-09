package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.request.BaseRequest
import vn.techres.line.data.model.request.DeleteAddressRequest

class DeleteAddressParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = DeleteAddressRequest()
}