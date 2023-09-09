package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class RealtimeBillOrder: Serializable {
    @JsonField(name = ["id"])
    var id : Int? = 0
}