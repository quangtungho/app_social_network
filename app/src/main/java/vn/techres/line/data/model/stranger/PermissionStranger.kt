package vn.techres.line.data.model.stranger

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class PermissionStranger : Serializable{
    @JsonField(name = ["is_call_phone"])
    var is_call_phone: Int? = 0

    @JsonField(name = ["is_call_video"])
    var is_call_video: Int? = 0
}