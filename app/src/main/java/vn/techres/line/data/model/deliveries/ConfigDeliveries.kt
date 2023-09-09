package vn.techres.line.data.model.deliveries

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ConfigDeliveries : Serializable {

    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["access_token"])
    var access_token: String? = ""

    @JsonField(name = ["api_domain"])
    var api_domain: String? = ""

    @JsonField(name = ["api_key"])
    var api_key: String? = ""

    @JsonField(name = ["shipping_fee"])
    var shipping_fee: Double? = 0.0

    @JsonField(name = ["is_available"])
    var is_available: Int? = 0
}