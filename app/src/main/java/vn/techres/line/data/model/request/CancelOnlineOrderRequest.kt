package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class CancelOnlineOrderRequest : Serializable {
    @JsonField(name = ["note"])
    var note: String? = ""
}