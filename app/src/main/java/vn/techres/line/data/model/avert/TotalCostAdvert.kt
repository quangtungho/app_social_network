package vn.techres.line.data.model.avert

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable
import java.math.BigDecimal

/**
 * @Author: Phạm Văn Nhân
 * @Date: 1/7/2022
 */
@JsonObject
class TotalCostAdvert : Serializable {

    @JsonField(name = ["advert_id"])
    var advert_id: Int? = 0

    @JsonField(name = ["total_cost"])
    var total_cost: BigDecimal? = BigDecimal.ZERO

    @JsonField(name = ["total_cost_to_point"])
    var total_cost_to_point: Int? = 0

    @JsonField(name = ["total_point"])
    var total_point: Int? = 0

}