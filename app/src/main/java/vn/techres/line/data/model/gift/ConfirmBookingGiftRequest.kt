package vn.techres.line.data.model.gift

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class ConfirmBookingGiftRequest : Serializable {
    @JsonField(name = ["booking_id"])
    var booking_id: Int? = 0

    @JsonField(name = ["food_id"])
    var food_id: Int? = 0

    @JsonField(name = ["quantity"])
    var quantity: Double? = 0.0

    @JsonField(name = ["confirm_status"])
    var confirm_status: Int? = 0
}