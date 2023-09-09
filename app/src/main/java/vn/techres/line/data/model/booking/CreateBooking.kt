package vn.techres.line.data.model.booking

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.Food
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CreateBooking : Serializable {

    @JsonField(name = ["id"])
    var id: Int = 0

    @JsonField(name = ["branch_id"])
    var branch_id: Int = 0

    @JsonField(name = ["branch_picture"])
    var branch_picture: String = ""

    @JsonField(name = ["branch_name"])
    var branch_name: String = ""

    @JsonField(name = ["branch_address"])
    var branch_address: String = ""

    @JsonField(name = ["branch_phone"])
    var branch_phone: String = ""

    @JsonField(name = ["booking_status"])
    var booking_status: Int = 0

    @JsonField(name = ["booking_status_text"])
    var booking_status_text: String = ""

    @JsonField(name = ["total_amount"])
    var total_amount: Double = 0.0

    @JsonField(name = ["booking_time"])
    var booking_time: String = ""

    @JsonField(name = ["booking_status_name"])
    var booking_status_name: String = ""

    @JsonField(name = ["orther_requirements"])
    var orther_requirements: String = ""

    @JsonField(name = ["note"])
    var note: String = ""

    @JsonField(name = ["deposit_amount"])
    var deposit_amount: Double = 0.0

    @JsonField(name = ["receive_deposit_time"])
    var receive_deposit_time: String = ""

    @JsonField(name = ["return_deposit_amount"])
    var return_deposit_amount: Double = 0.0

    @JsonField(name = ["return_deposit_time"])
    var return_deposit_time: String = ""

    @JsonField(name = ["created_at"])
    var created_at: String = ""

    @JsonField(name = ["number_slot"])
    var number_slot: Int = 0

    @JsonField(name = ["foods"])
    var foods = ArrayList<Food>()
}
