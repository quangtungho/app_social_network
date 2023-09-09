package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.ImageVideo
import vn.techres.line.data.model.response.BaseResponse

class ImageVideoResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<ImageVideo>()
}