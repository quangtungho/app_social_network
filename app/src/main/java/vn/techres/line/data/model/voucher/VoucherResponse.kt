package vn.techres.line.data.model.voucher

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.response.BaseResponse

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class VoucherResponse : BaseResponse(){
    @JsonField(name=["data"])
    var data = VoucherData()
}