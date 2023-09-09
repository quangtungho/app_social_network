package vn.techres.line.data.model.avert

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class AdvertContractRequest : Serializable {
    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["phone"])
    var phone: String? = ""

    @JsonField(name = ["email"])
    var email: String? = ""

    @JsonField(name = ["address"])
    var address: String? = ""

    @JsonField(name = ["company_name"])
    var company_name: String? = ""

    @JsonField(name = ["note"])
    var note: String? = ""
}