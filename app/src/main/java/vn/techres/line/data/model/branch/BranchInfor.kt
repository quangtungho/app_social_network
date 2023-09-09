package vn.techres.line.data.model.branch

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class BranchInfor : Serializable {
    @JsonField(name = ["branch_name"])
    var branch_name: String? = ""

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["branch_rating"])
    var branch_rating: Float? = 0F

    @JsonField(name = ["branch_address"])
    var branch_address: String? = ""

    @JsonField(name = ["branch_logo"])
    var branch_logo: String? = ""

    @JsonField(name = ["branch_review_count"])
    var branch_review_count: Int? = 0

    @JsonField(name = ["phone_number"])
    var phone_number: String? = ""
}