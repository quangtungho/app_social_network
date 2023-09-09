package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.Point

class PointRespone: BaseResponse() {
    @JsonField(name = ["data"])
    var data = Point()
}