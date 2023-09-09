package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.RestaurantSocialContentsData

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class RestaurantSocialContentsResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data: RestaurantSocialContentsData? = null
}