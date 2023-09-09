package vn.techres.line.data.model.deliveries

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class DeliveriesData : Serializable {
    @JsonField(name = ["third_party_delivery_list"])
    var delivery = ArrayList<ConfigDeliveries>()
}