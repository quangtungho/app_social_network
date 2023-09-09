package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.LevelValue

class LevelValueResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<LevelValue>()
}