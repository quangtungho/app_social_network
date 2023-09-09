package vn.techres.line.data.model.avert

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 1/7/2022
 */
class TotalCountPlayAdverts : Serializable {

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["address_full_text"])
    var address_full_text: String? = ""

    @JsonField(name = ["total_count_play"])
    var total_count_play: Int? = 0

    @JsonField(name = ["total_point_used"])
    var total_point_used: Int? = 0

    @JsonField(name = ["total_point"])
    var total_point: Int? = 0

    @JsonField(name = ["total_cost"])
    var total_cost: Double? = 0.0

}