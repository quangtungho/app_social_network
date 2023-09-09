package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.MessageViewer
import vn.techres.line.data.model.response.BaseResponse

class MessageViewerResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<MessageViewer>()
}