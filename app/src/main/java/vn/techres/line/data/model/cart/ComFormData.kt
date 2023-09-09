package vn.techres.line.data.model.cart

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.ItemCart
import java.io.Serializable

class ComFormData : Serializable {
    @JsonField(name = ["discount"])
    var discount: Int? = 0

    @JsonField(name = ["amount"])
    var amount: Float? = 0F

    @JsonField(name = ["total_amount"])
    var total_amount: Float? = 0F

    @JsonField(name = ["discount_by_voucher"])
    var discount_by_voucher: Int? = 0

    @JsonField(name = ["delivery_fee"])
    var delivery_fee: Int? = 0

    @JsonField(name = ["total_discount"])
    var total_discount: Float? = 0F

    @JsonField(name = ["total_point"])
    var total_point: Int? = 0

    @JsonField(name = ["list"])
    var list = ArrayList<ItemCart>()
}