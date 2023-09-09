package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.Tags

class CommentNewFeedRequest {
    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["image_urls"])
    var image_urls = ArrayList<String>()

    @JsonField(name = ["customer_tags"])
    var customer_tags: Tags? = null

    @JsonField(name = ["customer_new_id"])
    var customer_new_id = Tags()
}