package vn.techres.line.data.model.gift

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.response.BaseResponse

/**
 * @Author: Phạm Văn Nhân
 * @Date: 1/14/2022
 */

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class GiftDetailResponse : BaseResponse(){
    @JsonField(name=["data"])
    var data = GiftDetail()

}