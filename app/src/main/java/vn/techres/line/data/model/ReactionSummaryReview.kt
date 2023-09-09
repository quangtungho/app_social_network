package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class ReactionSummaryReview : Serializable {
    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["reaction_id"])
    var reaction_id: Int? = 0


    @JsonField(name = ["icon"])
    var icon: String? = ""

    @JsonField(name = ["number"])
    var number: Int? = 0

    @JsonField(name = ["value"])
    var value: Int? = 0
}