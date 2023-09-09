package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.RestaurantSloganData

class RestaurantSloganResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data =  RestaurantSloganData()
}