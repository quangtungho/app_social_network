package vn.techres.line.data.model.restaurant

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable
import java.util.ArrayList

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class RestaurantCard : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["point"])
    var point: Int? = 0

    @JsonField(name = ["prefix"])
    var prefix: String? = ""

    @JsonField(name = ["accumulate_point"])
    var accumulate_point: Int? = 0

    @JsonField(name = ["promotion_point"])
    var promotion_point: Int? = 0

    @JsonField(name = ["alo_point"])
    var alo_point: Int? = 0

    @JsonField(name = ["total_all_point"])
    var total_all_point: Int? = 0

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["normalize_name"])
    var normalize_name: String? = ""

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String? = ""

    @JsonField(name = ["restaurant_info"])
    var restaurant_info: String? = ""

    @JsonField(name = ["restaurant_avatar_three_image"])
    var restaurant_avatar_three_image: Avatar? = null

    @JsonField(name = ["restaurant_membership_card_name"])
    var restaurant_membership_card_name: String? = ""

    @JsonField(name = ["restaurant_membership_card_color_hex_code"])
    var restaurant_membership_card_color_hex_code: String? = ""

    @JsonField(name = ["restaurant_address"])
    var restaurant_address: String? = ""

    @JsonField(name = ["branch_count"])
    var branch_count: Int? = 0

    @JsonField(name = ["average_rate"])
    var average_rate: Double? = 0.0

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["updated_at"])
    var updated_at: String? = ""

    @JsonField(name = ["restaurant_phone"])
    var phone: String? = ""

    @JsonField(name = ["restaurant_image_urls"])
    var restaurant_image_urls = ArrayList<String>()

    // Phần trăm tối đa điểm khuyến mãi được sử dụng trên hóa đơn dựa vào số tiền trên hóa đơn
    @JsonField(name = ["maximum_percent_order_amount_to_promotion_point_allow_use_in_each_bill"])
    var maximum_percent_order_amount_to_promotion_point_allow_use_in_each_bill: Int? = 0

    // Phần trăm tối đa điểm Alo Point được sử dụng trên hóa đơn dựa vào số tiền trên hóa đơn
    @JsonField(name = ["maximum_percent_order_amount_to_alo_point_allow_use_in_each_bill"])
    var maximum_percent_order_amount_to_alo_point_allow_use_in_each_bill: Int? = 0

    // Số tiền tối đa quy đổi sang alo point giới hạn được phép sử dụng trên mỗi hóa đơn, config này là chặn giới hạn của config theo phần trăm
    @JsonField(name = ["maximum_money_by_alo_point_allow_use_in_each_bill"])
    var maximum_money_by_alo_point_allow_use_in_each_bill: Float? = 0F
}