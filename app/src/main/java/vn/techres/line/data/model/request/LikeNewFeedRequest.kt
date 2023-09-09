package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField

class LikeNewFeedRequest {
    @JsonField(name = ["id"])
    var id: Int? = 0
}