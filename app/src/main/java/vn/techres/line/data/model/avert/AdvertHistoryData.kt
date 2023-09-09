package vn.techres.line.data.model.avert

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 1/7/2022
 */
@JsonObject
class AdvertHistoryData : Serializable {

    @JsonField(name = ["total_cost_advert"])
    var total_cost_advert = TotalCostAdvert()

    @JsonField(name = ["total_count_play_adverts"])
    var total_count_play_adverts = ArrayList<TotalCountPlayAdverts>()
}