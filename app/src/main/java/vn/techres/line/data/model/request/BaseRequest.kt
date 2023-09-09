package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.helper.AppConfig

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
open class BaseRequest {
    @JsonField(name = ["request_url"])
    var request_url: String? = ""

    @JsonField(name = ["project_id"])
    var project_id: Int? = AppConfig.PROJECT_ALOLINE

    @JsonField(name = ["http_method"])
    var http_method: Int? = 0

    @JsonField(name = ["os_name"])
    var os_name: String? = "android"

    @JsonField(name = ["is_production_mode"])
    var is_production_mode: Int? = AppConfig.getProductionMode()
}