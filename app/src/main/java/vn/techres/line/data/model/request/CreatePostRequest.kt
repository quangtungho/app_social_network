package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.Tags

class CreatePostRequest {

    @JsonField(name = ["title"])
    var title: String? = ""

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["image_urls"])
    var image_urls: List<String>? = null

    @JsonField(name = ["customer_tags"])
    var customer_tags: Tags? = null
}