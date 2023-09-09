package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.math.BigDecimal

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class OrderCustomer : Serializable{

    @JsonField(name=["id"])
    var id : Int? = 0

    @JsonField(name=["vat"])
    var vat : Double? = 0.0

    @JsonField(name=["amount"])
    var amount : BigDecimal? = BigDecimal.ZERO

    @JsonField(name=["payment_day"])
    var payment_day : String? = ""

    @JsonField(name=["table_name"])
    var table_name : String? = ""

    @JsonField(name=["employee_name"])
    var employee_name : String? = ""

    @JsonField(name=["total_amount"])
    var total_amount : BigDecimal? = BigDecimal.ZERO

    @JsonField(name=["discount_percent"])
    var discount_percent : Double? = 0.0

    @JsonField(name=["discount_amount"])
    var discount_amount : BigDecimal? = BigDecimal.ZERO

    @JsonField(name=["vat_amount"])
    var vat_amount : BigDecimal? = BigDecimal.ZERO

    @JsonField(name=["point"])
    var point : Int? = 0

    @JsonField(name=["membership_point_used"])
    var membership_point_used : Int? = 0

    @JsonField(name=["membership_point_used_added"])
    var membership_point_added : Int? = 0

    @JsonField(name=["membership_point_used_amount"])
    var membership_point_used_amount : Double? = 0.0

    @JsonField(name=["order_details"])
    var order_details = ArrayList<OrderDetail>()

    @JsonField(name=["branch_id"])
    var branch_id : Int? = 0

    @JsonField(name=["table_id"])
    var table_id : Int? = 0

    @JsonField(name=["employee_id"])
    var employee_id : Int? = 0

    @JsonField(name=["order_status"])
    var order_status : Int? = 0

    @JsonField(name=["order_status_name"])
    var order_status_name : String? = ""

    @JsonField(name=["branch_name"])
    var branch_name : String? = ""

    @JsonField(name=["created_at"])
    var created_at : String? = ""

    @JsonField(name=["shipping_receiver_name"])
    var shipping_receiver_name : String? = ""

    @JsonField(name=["shipping_address"])
    var shipping_address : String? = ""

    @JsonField(name=["shipping_phone"])
    var shipping_phone : String? = ""

    @JsonField(name=["shipping_fee"])
    var shipping_fee : Double? = 0.0

    @JsonField(name=["is_online"])
    var is_online : Int? = 0

}