package vn.techres.line.data.model.bill

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class FoodVATData : Serializable {
    @JsonField(name=["vat_percent"])
    var vat_percent : Double = 0.0

    @JsonField(name=["vat_amount"])
    var vat_amount : Double = 0.0

    @JsonField(name=["order_details"])
    var order_details = ArrayList<FoodVATDataDetail>()
}