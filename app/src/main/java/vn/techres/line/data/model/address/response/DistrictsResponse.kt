package vn.techres.line.data.model.address.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.address.DistrictData
import vn.techres.line.data.model.response.BaseResponse

class DistrictsResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data = DistrictData()
}