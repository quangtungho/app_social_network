package vn.techres.line.data.model.stranger.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.request.BaseRequest
import vn.techres.line.data.model.stranger.PermissionStranger

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class PermissionStrangerParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = PermissionStranger()
}