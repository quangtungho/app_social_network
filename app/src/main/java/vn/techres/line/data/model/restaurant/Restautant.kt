package vn.techres.line.data.model.restaurant

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.utils.Logo
import java.io.Serializable

@JsonObject
class Restautant : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["brand_name"])
    var brand_name: String? = ""

    @JsonField(name = ["logo"])
    var logo = Logo()
}