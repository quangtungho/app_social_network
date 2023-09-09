package vn.techres.line.data.model.restaurant.data

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.restaurant.RestaurantRegister
import java.io.Serializable

class RestaurantRegisterData : Serializable {

    @JsonField(name = ["limit"])
    var limit : Int? = 0

    @JsonField(name = ["list"])
    var list = ArrayList<RestaurantRegister>()

    @JsonField(name = ["total_record"])
    var total_record : Int? = 0

}