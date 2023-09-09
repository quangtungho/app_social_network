package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class ListPointHistoryRequest {
    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["page"])
    var page: Int? = 0

    @JsonField(name = ["limit"])
    var limit: Int? = 0
}