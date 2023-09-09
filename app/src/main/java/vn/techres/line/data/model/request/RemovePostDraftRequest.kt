package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class RemovePostDraftRequest {
    @JsonField(name = ["_id"])
    var _id: String? = ""

}

