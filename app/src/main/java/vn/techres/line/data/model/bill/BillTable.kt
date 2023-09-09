package vn.techres.line.data.model.bill

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.OrderDetailBill
import java.io.Serializable
import java.math.BigDecimal

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BillTable : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["vat"]) //Phần trăm thuế
    var vat: Double? = 0.0

    @JsonField(name = ["discount_percent"]) //Phần trăm giảm giá
    var discount_percent: Double? = 0.0

    @JsonField(name = ["vat_amount"]) //Số tiền tiền thuế
    var vat_amount: BigDecimal? = BigDecimal.ZERO

    @JsonField(name = ["discount_amount"]) //Số tiền giảm giá
    var discount_amount: BigDecimal? = BigDecimal.ZERO

    @JsonField(name = ["total_amount"]) //tổng tiền thanh toán
    var total_amount: BigDecimal? = BigDecimal.ZERO

    @JsonField(name = ["total_final_amount"]) //tổng tiền thanh toán
    var total_final_amount: BigDecimal? = BigDecimal.ZERO

    @JsonField(name = ["amount"]) //Thành tiền
    var amount: BigDecimal? = BigDecimal.ZERO

    @JsonField(name = ["order_details"]) // Chi tiết bill
    var order_details = ArrayList<OrderDetailBill>()

    @JsonField(name = ["order_detail_points"]) // Chi tiết bill
    var order_detail_points = ArrayList<OrderDetailBill>()

    @JsonField(name = ["order_status"]) //Số tiền tiền thuế
    var order_status: Int? = 0

    @JsonField(name = ["membership_point_used_amount"]) //Số điểm được sử dụng
    var membership_point_used_amount: Int? = 0

    @JsonField(name = ["membership_point_added"]) //Điểm cộng từ hóa đơn
    var membership_point_added: Int? = 0

    @JsonField(name = ["table_name"]) //Tên bàn
    var table_name: String? = ""

    @JsonField(name = ["payment_day"]) //Ngày thanh toán
    var payment_day: String? = ""

    @JsonField(name = ["employee_name"])
    var employee_name: String? = ""

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["maximum_point_allow_use_in_each_bill"]) //  số điểm nạp tối đa được phép sử dụng trên bill
    var maximum_point_allow_use_in_each_bill: Int? = 0

    @JsonField(name = ["maximum_accumulate_point_allow_use_in_each_bill"]) //  số điểm tích lũy tối đa được phép sử dụng trên bill
    var maximum_accumulate_point_allow_use_in_each_bill: Int? = 0

    @JsonField(name = ["maximum_promotion_point_allow_use_in_each_bill"]) //  số điểm khuyến mãi tối đa được phép sử dụng trên bill
    var maximum_promotion_point_allow_use_in_each_bill: Int? = 0

    @JsonField(name = ["maximum_alo_point_allow_use_in_each_bill"])  //  số điểm giá trị tối đa được phép sử dụng trên bill
    var maximum_alo_point_allow_use_in_each_bill: Int? = 0

    @JsonField(name = ["membership_point_used"])  //  điểm nạp tiền đã sử dụng
    var membership_point_used: Int? = 0

    @JsonField(name = ["membership_accumulate_point_used"])  // điểm tích lũy đã sử dụng
    var membership_accumulate_point_used: Int? = 0

    @JsonField(name = ["membership_promotion_point_used"]) // điểm khuyến mãi đã sử dụng
    var membership_promotion_point_used: Int? = 0

    @JsonField(name = ["membership_alo_point_used"]) // điểm giá trị value đã sử dụng
    var membership_alo_point_used: Int? = 0

    @JsonField(name = ["membership_total_point_used"]) // tổng điểm đã sử dụng
    var membership_total_point_used: Int? = 0

    @JsonField(name = ["customer_name"])
    var customer_name: String? = ""

    @JsonField(name = ["customer_phone"])
    var customer_phone: String? = ""

    @JsonField(name = ["total_point_used"]) //Tổng điểm sử dụng(đang chờ xác nhận)
    var total_point_used: Int? = 0

    @JsonField(name = ["buy_with_point"])
    var buy_with_point: Int? = 0

    @JsonField(name = ["is_allow_use_point"]) // trạng thái sử dụng điểm
    var is_allow_use_point: Int? = 0

}