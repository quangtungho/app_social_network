package vn.techres.line.data.model.branch

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Logo
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class  SaveBranch : Serializable {
    @JsonField(name = ["branch_name"])
    var branch_name: String? = ""

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["branch_rating"])
    var branch_rating: Double? = 0.0

    @JsonField(name = ["branch_address"])
    var branch_address: String? = ""

    @JsonField(name = ["branch_logo"])
    var branch_logo = Logo()

    @JsonField(name = ["branch_review_count"])
    var branch_review_count: Int? = 0
}