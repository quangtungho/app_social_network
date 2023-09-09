package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.data.LinkMediaChatData
import vn.techres.line.data.model.response.BaseResponse
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class LinkMediaChatResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data : LinkMediaChatData? = null
}