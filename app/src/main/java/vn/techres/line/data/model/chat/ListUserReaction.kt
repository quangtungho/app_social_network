package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField

class ListUserReaction {
    @JsonField(name = ["list_user"])
    var list_user : ArrayList<UserReaction> = ArrayList()
    @JsonField(name = ["total_reaction"])
    var total_reaction : Int = 0
}