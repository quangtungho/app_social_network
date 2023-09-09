package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class CreateReceiveBirthdayRequest: Serializable {
    @JsonField(name = ["customer_id"])
    var customer_id : Int? = 0
    @JsonField(name = ["branch_id"])
    var branch_id : Int? = 0
    @JsonField(name = ["restaurant_birthday_gift_id"])
    var restaurant_birthday_gift_id : Int? = 0
    @JsonField(name = ["customer_name"])
    var customer_name : String? = ""
    @JsonField(name = ["customer_phone"])
    var customer_phone : String? = ""
    @JsonField(name = ["customer_address"])
    var customer_address : String? = ""
    @JsonField(name = ["gift"])
    var gift= ArrayList<String>()
}