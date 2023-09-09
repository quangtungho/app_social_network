package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.booking.BookingInformationData

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BookingFoodResponse : BaseResponse(){
    @JsonField(name =  ["data"])
    var data : BookingInformationData? = null
}