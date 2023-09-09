package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.response.MessageViewerResponse
import vn.techres.line.data.model.request.BaseRequest

class MessageViewerParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = MessageViewerResponse()
}