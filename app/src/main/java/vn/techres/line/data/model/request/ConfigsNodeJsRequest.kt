package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class ConfigsNodeJsRequest {
    @JsonField(name = ["secret_key"])
    var secret_key: String? = ""

    @JsonField(name = ["type"])
    var type: String? = ""
}