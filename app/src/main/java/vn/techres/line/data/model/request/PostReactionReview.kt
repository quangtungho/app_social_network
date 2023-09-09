package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class PostReactionReview : Serializable {
    @JsonField(name = ["reaction_id"])
    var reaction_id: Int? = 0
    @JsonField(name = ["branch_review_id"])
    var branch_review_id: String? = ""
    @JsonField(name = ["java_access_token"])
    var java_access_token: String? = ""
}