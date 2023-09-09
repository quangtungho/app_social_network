package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.OrderCustomer

class OrderDetailData : BaseResponse() {
    @JsonField(name=["data"])
    var data = OrderCustomer()
}