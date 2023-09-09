package vn.techres.line.data.model.chat.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.request.group.ViceGroupRequest
import vn.techres.line.data.model.request.BaseRequest
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ViceGroupParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = ViceGroupRequest()
}