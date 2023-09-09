package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class PointHistory:Serializable {
    @JsonField(name=["limit"])
    var limit:Int?=0
    @JsonField(name=["list"])
    var list = ArrayList<ListPointHistory>()
    @JsonField(name=["total_record"])
    var total_record:Int?=0

}