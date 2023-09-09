package vn.techres.line.data.model.restaurant.data

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.restaurant.RestaurantCard
import java.io.Serializable

class RestaurantCardData : Serializable {
    @JsonField(name = ["list"])
    var list = ArrayList<RestaurantCard>()

    @JsonField(name = ["total_record"])
    var total_record : Int? = 0

    @JsonField(name = ["limit"])
    var limit : Int? = 0
}