package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class UsingPointToOrderRequest {
    @JsonField(name = ["point"])
    var point: Int? = 0

    @JsonField(name = ["accumulate_point"])
    var accumulate_point: Int? = 0

    @JsonField(name = ["promotion_point"])
    var promotion_point: Int? = 0

    @JsonField(name = ["alo_point"])
    var alo_point: Int? = 0
}