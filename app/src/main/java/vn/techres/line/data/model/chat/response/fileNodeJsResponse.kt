package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.FileNodeJs
import vn.techres.line.data.model.response.BaseResponse

class FileNodeJsResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = FileNodeJs()
}