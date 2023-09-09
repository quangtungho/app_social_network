package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.newfeed.DetailReactionReview
import java.io.Serializable

class PeopleReactionRequest : Serializable {
    @JsonField(name = ["list_user"])
    var list_user =  ArrayList<DetailReactionReview>()
}