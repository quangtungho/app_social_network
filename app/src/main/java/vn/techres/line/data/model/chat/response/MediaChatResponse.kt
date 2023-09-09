package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.data.MediaChatData
import vn.techres.line.data.model.response.BaseResponse
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class MediaChatResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data : MediaChatData? = null
}