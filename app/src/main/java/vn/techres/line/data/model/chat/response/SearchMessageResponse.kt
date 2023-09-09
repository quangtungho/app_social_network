package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.SearchMessage
import vn.techres.line.data.model.response.BaseResponse

class SearchMessageResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<SearchMessage>()
}