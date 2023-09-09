package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.OrderCustomerData

class OrderCustomerResponse : BaseResponse() {
    @JsonField(name=["data"])
    var data = OrderCustomerData()
}