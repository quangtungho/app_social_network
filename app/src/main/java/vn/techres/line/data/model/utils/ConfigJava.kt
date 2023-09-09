package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ConfigJava {
    @JsonField(name = ["type"])
    var type: String? = ""

    @JsonField(name = ["version"])
    var version: String? = ""

    @JsonField(name = ["domain"])
    var domain: String? = ""

    @JsonField(name = ["api_key"])
    var api_key: String? = ""

    @JsonField(name = ["api_domain"])
    var api_domain: String? = ""

    @JsonField(name = ["chat_domain"])
    var chat_domain: String? = ""

    @JsonField(name = ["date_time"])
    var date_time: String? = ""

    @JsonField(name = ["realtime_domain"])
    var realtime_domain: String? = ""

    @JsonField(name = ["log_domain"])
    var log_domain: String? = ""

    @JsonField(name = ["api_oauth_node"])
    var api_oauth_node: String? = ""
}
