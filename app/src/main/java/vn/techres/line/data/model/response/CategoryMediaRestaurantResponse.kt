package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.CategoryMediaRestaurantData

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */

@JsonObject
class CategoryMediaRestaurantResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = CategoryMediaRestaurantData()
}