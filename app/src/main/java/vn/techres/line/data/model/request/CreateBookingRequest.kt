package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.booking.FoodOrder

class CreateBookingRequest {

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["orther_requirements"])
    var orther_requirements: String? = ""

    @JsonField(name = ["note"])
    var note: String? = ""

    @JsonField(name = ["number_slot"])
    var number_slot: Int? = 0

    @JsonField(name = ["food_request"])
    var food_request = ArrayList<FoodOrder>()

    @JsonField(name = ["booking_time"])
    var booking_time: String? = ""

}