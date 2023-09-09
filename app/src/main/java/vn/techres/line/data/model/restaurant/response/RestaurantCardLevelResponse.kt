package vn.techres.line.data.model.restaurant.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.restaurant.RestaurantCardLevel

class RestaurantCardLevelResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data  = ArrayList<RestaurantCardLevel>()
}