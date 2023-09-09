package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.RestaurantBranch

/**
 * @Author: Phạm Văn Nhân
 * @Date: 28/06/2022
 */
class RestaurantBranchResponse: BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<RestaurantBranch>()
}