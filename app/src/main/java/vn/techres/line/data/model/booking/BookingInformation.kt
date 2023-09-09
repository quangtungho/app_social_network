package vn.techres.line.data.model.booking

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.OrderFood
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BookingInformation : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = null

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = null

    @JsonField(name = ["branch_name"])
    var branch_name: String? = null

    @JsonField(name = ["branch_address"])
    var branch_address: String? = null

    @JsonField(name = ["branch_phone"])
    var branch_phone: String? = null

    @JsonField(name = ["booking_status"])
    var booking_status: Int? = null

    @JsonField(name = ["booking_status_text"])
    var booking_status_text: String? = null

    @JsonField(name = ["total_amount"])
    var total_amount: String? = null

    @JsonField(name = ["booking_time"])
    var booking_time: String? = null

    @JsonField(name = ["food_request"])
    var food_request = ArrayList<OrderFood>()

    @JsonField(name = ["orther_requirements"])
    var orther_requirements: String? = null

    @JsonField(name = ["note"])
    var note: String? = null

    @JsonField(name = ["number_slot"])
    var number_slot: Int? = null
}