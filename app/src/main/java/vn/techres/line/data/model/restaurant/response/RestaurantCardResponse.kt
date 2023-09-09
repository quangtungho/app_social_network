package vn.techres.line.data.model.restaurant.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.restaurant.data.RestaurantCardData
import vn.techres.line.data.model.response.BaseResponse
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class RestaurantCardResponse: BaseResponse() {
    @JsonField(name = ["data"])
    var data  = RestaurantCardData()
}