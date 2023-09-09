package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField

class LikeRequest {
    @JsonField(name = ["id"])
    var id: Int? = 0
}