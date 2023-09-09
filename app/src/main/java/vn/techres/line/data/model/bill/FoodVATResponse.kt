package vn.techres.line.data.model.bill

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.response.BaseResponse

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class FoodVATResponse  : BaseResponse(){
    @JsonField(name=["data"])
    var data = ArrayList<FoodVATData>()
}
