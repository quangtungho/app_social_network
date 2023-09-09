package vn.techres.line.data.model.newfeed

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.Tags
import vn.techres.line.data.model.utils.Avatar
import vn.techres.line.data.model.utils.Url
import java.io.Serializable

@JsonObject
class Comment: Serializable {
    @JsonField(name = ["comment_id"])
    var comment_id: String? = ""

    @JsonField(name = ["branch_review_id"])
    var branch_review_id: String? = ""

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["type"])
    var type: Int? = 0

    @JsonField(name = ["reply_comment"])
    var reply_comment = ArrayList<Comment>()

    @JsonField(name = ["customer_id"])
    var customer_id: Int? = 0

    @JsonField(name = ["customer_name"])
    var customer_name: String? = ""

    @JsonField(name = ["customer_avatar"])
    var customer_avatar = Avatar()

    @JsonField(name = ["reply_count"])
    var reply_count: Int? = 0

    @JsonField(name = ["customer_tags"])
    var customer_tags = ArrayList<Tags>()

    @JsonField(name = ["image_urls"])
    var image_urls = ArrayList<Url>()

    @JsonField(name = ["customer_like_ids"])
    var customer_like_ids = ArrayList<Int>()

    @JsonField(name = ["my_reaction_id"])
    var my_reaction_id: Int? = 0

    @JsonField(name = ["total_comment"])
    var total_comment: Int? = 0

    @JsonField(name = ["sticker"])
    var sticker: String? = ""

    @JsonField(name = ["customer_nick_name"])
    var customer_nick_name: String? = ""

    @JsonField(name = ["customer_is_display_nick_name"])
    var customer_is_display_nick_name: Int? = 0

    @JsonField(name = ["updated_at"])
    var updated_at: String? = ""

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["not_answered"])
    var not_answered: Boolean? = true

}