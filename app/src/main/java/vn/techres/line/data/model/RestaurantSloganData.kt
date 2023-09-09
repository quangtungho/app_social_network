package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable
import java.util.ArrayList

class RestaurantSloganData: Serializable {
    @JsonField(name = ["list"])
    var list =  ArrayList<RestaurantSlogan>()
    @JsonField(name = ["limit"])
    var limit : Int? = 0
    @JsonField(name = ["total_record"])
    var total_record : Int? = 0
}