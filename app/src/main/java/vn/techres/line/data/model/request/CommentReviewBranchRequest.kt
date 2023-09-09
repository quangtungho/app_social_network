package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.Tag
import vn.techres.line.data.model.utils.MediaPost


@JsonObject
class CommentReviewBranchRequest {

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["image_urls"])
    var image_urls = ArrayList<MediaPost>()

    @JsonField(name = ["sticker"])
    var sticker: String? = ""

    @JsonField(name = ["customer_tags"])
    var customer_tags = ArrayList<Tag>()

    @JsonField(name = ["branch_review_id"])
    var branch_review_id: String? = ""

    @JsonField(name = ["comment_id"])
    var comment_id: String? = ""

    @JsonField(name = ["type_edit_comment"])
    var type_edit_comment: String? = ""


}


