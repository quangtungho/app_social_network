package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.PointHistory

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class PointHistoryResponse: BaseResponse() {
    @JsonField(name=["data"])
    var data = PointHistory()
}