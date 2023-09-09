package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.RestaurantCocialContentsData

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class RestaurantCocialContentsResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data: RestaurantCocialContentsData? = null
}