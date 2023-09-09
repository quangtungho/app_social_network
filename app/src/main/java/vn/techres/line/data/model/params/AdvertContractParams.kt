package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.avert.AdvertContractRequest
import vn.techres.line.data.model.request.BaseRequest

@JsonObject
class AdvertContractParams : BaseRequest(){
    @JsonField(name = ["params"])
    var params = AdvertContractRequest()
}