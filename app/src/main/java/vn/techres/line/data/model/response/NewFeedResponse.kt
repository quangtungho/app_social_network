package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.NewFeedData

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class NewFeedResponse: BaseResponse() {
    @JsonField(name = ["data"])
    var data = NewFeedData()

}