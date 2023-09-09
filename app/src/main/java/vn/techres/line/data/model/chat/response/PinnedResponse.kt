package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.data.PinnedData
import vn.techres.line.data.model.response.BaseResponse

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class PinnedResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data : PinnedData? = null

}