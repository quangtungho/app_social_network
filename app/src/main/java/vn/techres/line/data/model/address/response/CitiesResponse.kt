package vn.techres.line.data.model.address.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.address.CityData
import vn.techres.line.data.model.response.BaseResponse

class CitiesResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data = CityData()
}