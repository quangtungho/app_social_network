package vn.techres.line.data.model.deliveries

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BundleDeliveries : Serializable {
    @JsonField(name = ["id"])
    var branch_id: Int? = 0

    @JsonField(name = ["lat"])
    var lat: Double? = 0.0

    @JsonField(name = ["long"])
    var lng: Double? = 0.0
}