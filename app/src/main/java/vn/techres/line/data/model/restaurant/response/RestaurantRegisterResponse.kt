package vn.techres.line.data.model.restaurant.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.restaurant.data.RestaurantRegisterData

@JsonObject
class RestaurantRegisterResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data  = RestaurantRegisterData()
}