package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.booking.CreateBooking

class CreateBookingResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data: CreateBooking? = null
}