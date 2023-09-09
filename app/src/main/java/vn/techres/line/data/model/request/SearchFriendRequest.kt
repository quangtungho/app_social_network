package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField

class SearchFriendRequest {
    @JsonField(name = ["keyword"])
    var keyword: String? = ""
}