package vn.techres.line.data.model.newfeed

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.Tags

class CommentNewFeed {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["customer_id"])
    var customer_id: Int? = 0

    @JsonField(name = ["customer_name"])
    var customer_name: String? = ""

    @JsonField(name = ["customer_avatar"])
    var customer_avatar: String? = ""

    @JsonField(name = ["customer_tags"])
    var customer_tags = ArrayList<Tags>()

    @JsonField(name = ["image_urls"])
    var image_urls: ArrayList<String>? = ArrayList()

    @JsonField(name = ["created_at"])
    var created_at: String? = ""
}