package vn.techres.line.data.model.food

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class FoodPurcharePointData: Serializable {
    @JsonField(name = ["list"])
    var list =  ArrayList<FoodPurcharePoint>()
    @JsonField(name = ["total_record"])
    var total_record : Int? = 0
    @JsonField(name = ["limit"])
    var limit : Int? = 0
}