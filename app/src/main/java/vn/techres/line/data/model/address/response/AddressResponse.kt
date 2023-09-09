package vn.techres.line.data.model.address.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.address.Address
import vn.techres.line.data.model.response.BaseResponse

class AddressResponse : BaseResponse() {

    @JsonField(name = ["data"])
    var data = ArrayList<Address>()
}