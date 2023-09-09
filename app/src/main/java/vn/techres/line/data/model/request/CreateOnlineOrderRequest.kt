package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.FoodOnlineOrder
import vn.techres.line.data.model.address.Address
import java.io.Serializable

class CreateOnlineOrderRequest : Serializable {

    @JsonField(name = ["branch_id"])
    var branch_id : Int? = 0

    @JsonField(name = ["third_party_delivery_id"])
    var third_party_delivery_id : Int? = 0

    @JsonField(name = ["shipping_address"])
    var shipping_address : Address? = null

    @JsonField(name = ["receiver_name"])
    var receiver_name : String? = ""


    @JsonField(name = ["receiver_phone"])
    var receiver_phone : String? = ""

    @JsonField(name = ["voucher"])
    var voucher : String? = ""

    @JsonField(name = ["note"])
    var note : String? = ""

    @JsonField(name = ["payment_method_id"])
    var payment_method_id : Int? = 0

    @JsonField(name = ["foods"])
    var foods = ArrayList<FoodOnlineOrder>()
}