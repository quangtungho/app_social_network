package vn.techres.line.data.model.branch.data

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.branch.Branch
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BranchData : Serializable {
    @JsonField(name = ["list"])
    var list = ArrayList<Branch>()
}