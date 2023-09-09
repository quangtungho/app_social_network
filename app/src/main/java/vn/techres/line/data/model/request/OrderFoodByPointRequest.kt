package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.ListOrderFoodByPoint
import java.io.Serializable

class OrderFoodByPointRequest: Serializable {

    @JsonField(name = ["order_id"])
    var order_id: Int? = 0

    @JsonField(name = ["foods"])
    var foods = ArrayList<ListOrderFoodByPoint>()
}