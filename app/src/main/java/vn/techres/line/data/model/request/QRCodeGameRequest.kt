package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class QRCodeGameRequest : Serializable {
    @JsonField(name = ["room_id"])
    var room_id: String? = ""

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0
}