package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class RateDetail : Serializable {
    @JsonField(name = ["rate_count"])
    var rate_count: Int? = 0

    @JsonField(name = ["average_rate"])
    var average_rate: Float? = 0.0f

    @JsonField(name = ["service_rate"])
    var service_rate: Float? = 0.0f

    @JsonField(name = ["food_rate"])
    var food_rate: Float? = 0.0f

    @JsonField(name = ["price_rate"])
    var price_rate: Float? = 0.0f

    @JsonField(name = ["space_rate"])
    var space_rate: Float? = 0.0f

    @JsonField(name = ["hygiene_rate"])
    var hygiene_rate: Float? = 0.0f
}