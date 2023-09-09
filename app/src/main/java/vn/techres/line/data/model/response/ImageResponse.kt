package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.newfeed.LinkImage

class ImageResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data : LinkImage? = null
}