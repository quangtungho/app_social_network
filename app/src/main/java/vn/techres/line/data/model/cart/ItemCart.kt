package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable

class ItemCart : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: Avatar? = null

    @JsonField(name = ["quantity"])
    var quantity: Int? = 0

    @JsonField(name = ["price"])
    var price: Int? = 0

    @JsonField(name = ["note"])
    var note: String? = ""

    @JsonField(name = ["total_price"])
    var total_price: Int? = 0
}