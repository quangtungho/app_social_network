package vn.techres.line.data.model.restaurant

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class UpdateRestaurantRequest : Serializable {
    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0
}