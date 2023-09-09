package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.request.BaseRequest
import vn.techres.line.data.model.request.CancelBookingRequest

class CancelBookingParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = CancelBookingRequest()
}