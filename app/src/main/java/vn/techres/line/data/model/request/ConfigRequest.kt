package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class ConfigRequest{
    @JsonField(name = ["project_id"])
    var project_id: String? = null
}