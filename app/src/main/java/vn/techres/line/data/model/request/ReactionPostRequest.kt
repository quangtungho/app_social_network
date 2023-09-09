package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class ReactionPostRequest : Serializable {
    @JsonField(name = ["reaction_id"])
    var reaction_id : Int = 0
}