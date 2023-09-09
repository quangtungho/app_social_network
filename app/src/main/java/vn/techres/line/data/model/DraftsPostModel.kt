package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.data.line.model.PostReview
import java.io.Serializable

@JsonObject
class DraftsPostModel : Serializable {
    @JsonField(name = ["limit"])
    var limit: Int? = 0

    @JsonField(name = ["list"])
    var list = ArrayList<PostReview>()

    @JsonField(name = ["total_record"])
    var total_record: Int? = 0

}