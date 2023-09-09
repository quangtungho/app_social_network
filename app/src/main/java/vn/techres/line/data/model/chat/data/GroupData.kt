package vn.techres.line.data.model.chat.data

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.Group
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class GroupData: Serializable {

    @JsonField(name = ["list"])
    var list = ArrayList<Group>()

    @JsonField(name = ["total_records"])
    var total_records : Int?= 0
}