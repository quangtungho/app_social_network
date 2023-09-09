package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.Tags


@JsonObject
class CommentReviewRestaurantRequest {
    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["image_urls"])
    var image_urls: List<String>? = null

    @JsonField(name = ["customer_tags"])
    var customer_tags: Tags? = null
}


