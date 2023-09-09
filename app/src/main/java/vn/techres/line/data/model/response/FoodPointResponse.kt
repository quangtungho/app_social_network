package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.food.FoodPurcharePointData

class FoodPointResponse: BaseResponse() {
    @JsonField(name = ["data"])
    var data: FoodPurcharePointData? = null

    @JsonField(name = ["total_record"])
    var total_record: Int? = 0

    @JsonField(name = ["limit"])
    var limit: Int? = 0
}