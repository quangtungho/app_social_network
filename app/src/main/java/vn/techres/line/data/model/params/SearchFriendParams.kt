package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.request.BaseRequest
import vn.techres.line.data.model.request.SearchFriendRequest

class SearchFriendParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = SearchFriendRequest()
}