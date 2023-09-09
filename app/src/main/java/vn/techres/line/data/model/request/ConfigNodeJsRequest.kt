package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ConfigNodeJsRequest {
    @JsonField(name = ["project_id"])
    var project_id: String? = null
}