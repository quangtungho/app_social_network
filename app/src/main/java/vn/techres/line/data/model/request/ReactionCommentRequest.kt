package vn.techres.line.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class ReactionCommentRequest : Serializable {
    @JsonField(name = ["list_user"])
    var list_user_id =  ArrayList<Int>()
}