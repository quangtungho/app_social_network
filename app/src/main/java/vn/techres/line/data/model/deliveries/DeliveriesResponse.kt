package vn.techres.line.data.model.deliveries

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.response.BaseResponse

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class DeliveriesResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<ConfigDeliveries>()
}