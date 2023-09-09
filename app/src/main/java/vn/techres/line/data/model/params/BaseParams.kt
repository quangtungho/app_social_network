package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.utils.Base
import vn.techres.line.data.model.request.BaseRequest

@JsonObject
class BaseParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = Base.create()
}