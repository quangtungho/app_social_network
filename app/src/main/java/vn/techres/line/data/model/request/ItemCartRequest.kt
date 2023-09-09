package vn.techres.line.data.model.request
import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.FoodOnlineOrder
import java.io.Serializable

class ItemCartRequest : Serializable {
    @JsonField(name=["branch_id"])
    var branch_id : Int?= 0

    @JsonField(name=["foods"])
    var foods = ArrayList<FoodOnlineOrder>()
}