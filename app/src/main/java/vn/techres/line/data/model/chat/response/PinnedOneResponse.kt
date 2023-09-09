package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.response.BaseResponse

class PinnedOneResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data : MessagesByGroup? = null
}