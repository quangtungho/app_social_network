package vn.techres.line.data.model.address.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.address.WardsData
import vn.techres.line.data.model.response.BaseResponse
class WardResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data = WardsData()
}