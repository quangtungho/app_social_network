package vn.techres.line.data.model.address

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Address : Serializable {

    @JsonField(name = ["id"])
    var id : Int? = 0

    @JsonField(name = ["lat"])
    var lat : Double? = 0.0

    @JsonField(name = ["lng"])
    var lng : Double? = 0.0

    @JsonField(name = ["name"])
    var name : String? = ""

    @JsonField(name = ["note"])
    var note : String? = ""

    @JsonField(name = ["address_full_text"])
    var address_full_text : String? = ""

    @JsonField(name=["type"])
    var type : Int? = 0

    @JsonField(name=["is_default"])
    var is_default : Int? = 0
}