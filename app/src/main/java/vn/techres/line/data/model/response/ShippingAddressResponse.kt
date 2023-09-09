package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.address.Address

class ShippingAddressResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data: Address? = null
}