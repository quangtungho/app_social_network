package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ConfigNodeJs : Serializable {
    @JsonField(name = ["api_key"])
    var api_key: String? = ""

    @JsonField(name = ["api_chat_aloline"])
    var api_chat_aloline: String? = ""

    @JsonField(name = ["api_lucky_wheel"])
    var api_lucky_wheel: String? = ""

    @JsonField(name = ["api_ads"])
    var api_ads: String? = ""

    @JsonField(name = ["api_oauth_node"])
    var api_oauth_node: String? = ""
}