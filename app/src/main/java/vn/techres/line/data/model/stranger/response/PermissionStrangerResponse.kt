package vn.techres.line.data.model.stranger.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.stranger.PermissionStranger
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class PermissionStrangerResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data : PermissionStranger? = null

}