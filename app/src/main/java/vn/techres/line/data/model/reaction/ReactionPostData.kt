package vn.techres.line.data.model.reaction

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class ReactionPostData : Serializable {
    @JsonField(name = ["limit"])
    var limit : Int = 0

    @JsonField(name = ["total_record"])
    var total_record : Int = 0

    @JsonField(name = ["list_user_reaction"])
    var list_user_reaction = ArrayList<InteractiveUser>()
}