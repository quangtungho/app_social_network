package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField

class LikeResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var id: Int? = null
}