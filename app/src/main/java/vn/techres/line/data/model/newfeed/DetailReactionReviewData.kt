package vn.techres.line.data.model.newfeed

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class DetailReactionReviewData: Serializable {
    @JsonField(name = ["list"])
    var list =  ArrayList<DetailReactionReview>()
    @JsonField(name = ["limit"])
    var limit : Int? = 0
    @JsonField(name = ["total_record"])
    var total_record : Int? = 0
}