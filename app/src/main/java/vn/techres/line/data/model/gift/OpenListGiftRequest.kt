package vn.techres.line.data.model.gift

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class OpenListGiftRequest : Serializable {
//    @JsonField(name = ["restaurant_id"])
//    var restaurant_id: Int? = 0

    @JsonField(name = ["os_name"])
    var os_name: String? = ""
}