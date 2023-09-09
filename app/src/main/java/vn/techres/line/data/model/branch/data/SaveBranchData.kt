package vn.techres.line.data.model.branch.data

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.branch.SaveBranch
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class SaveBranchData : Serializable {
    @JsonField(name = ["list"])
    var list = ArrayList<SaveBranch>()

    @JsonField(name = ["limit"])
    var limit : Int = 0

    @JsonField(name = ["total_record"])
    var total_record : Int = 0
}