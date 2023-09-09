package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class PeopleReaction: Serializable {
    @JsonField(name = ["userId"])
    var userId : Int? = 0
    @JsonField(name = ["reaction_id"])
    var reaction_id: Int? = 0
}