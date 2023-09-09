package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.OrderReviewData

@JsonObject
class OrderReviewResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data: OrderReviewData? = null
}