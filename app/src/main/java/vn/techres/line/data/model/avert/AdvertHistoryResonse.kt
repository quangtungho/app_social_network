package vn.techres.line.data.model.avert

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.response.BaseResponse

/**
 * @Author: Phạm Văn Nhân
 * @Date: 1/7/2022
 */

@JsonObject
class AdvertHistoryResonse : BaseResponse(){
    @JsonField(name = ["data"])
    var data  = AdvertHistoryData()
}