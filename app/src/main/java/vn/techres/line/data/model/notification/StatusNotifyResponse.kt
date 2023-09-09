package vn.techres.line.data.model.notification

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.response.BaseResponse

class StatusNotifyResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data: Int? = 0
}