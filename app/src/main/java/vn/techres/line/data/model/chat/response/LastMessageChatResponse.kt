package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.LastMessageChat
import vn.techres.line.data.model.response.BaseResponse

class LastMessageChatResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<LastMessageChat>()
}