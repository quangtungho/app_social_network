package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class PostReactionComment : Serializable {
    @JsonField(name = ["branch_review_id"])
    var branch_review_id: String? = ""

    @JsonField(name = ["comment_id"])
    var comment_id: String? = ""
}