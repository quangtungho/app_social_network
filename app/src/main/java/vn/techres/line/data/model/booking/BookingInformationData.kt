package vn.techres.line.data.model.booking

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.response.BaseResponse
import java.util.ArrayList

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BookingInformationData : BaseResponse() {
    @JsonField(name = ["list"])
    var list = ArrayList<BookingInformation>()
}