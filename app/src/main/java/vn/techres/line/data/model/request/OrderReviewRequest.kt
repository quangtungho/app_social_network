package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class OrderReviewRequest {
    @JsonField(name = ["service_rate"])
    var service_rate: Int? = 0

    @JsonField(name = ["food_rate"])
    var food_rate: Int? = 0

    @JsonField(name = ["price_rate"])
    var price_rate: Int? = 0

    @JsonField(name = ["space_rate"])
    var space_rate: Int? = 0

    @JsonField(name = ["hygiene_rate"])
    var hygiene_rate: Int? = 0
}