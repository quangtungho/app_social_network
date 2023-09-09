package vn.techres.line.data.model.restaurant.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.restaurant.RestaurantCard

class RestaurantCardDetailResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data  = RestaurantCard()
}