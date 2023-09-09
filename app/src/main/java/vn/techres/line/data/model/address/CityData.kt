package vn.techres.line.data.model.address

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CityData : Serializable {
    @JsonField(name = ["list"])
    var list = ArrayList<Cities>()
}