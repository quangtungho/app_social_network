package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class ReviewRestaurantRequest {
    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["title"])
    var title: String? = null

    @JsonField(name = ["rate"])
    var rate: Double? = 0.0

    @JsonField(name = ["content"])
    var content: String? = null

    @JsonField(name = ["image_urls"])
    var image_urls: List<String>? = null
}

