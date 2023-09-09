package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */

@JsonObject
class CategoryMediaRestaurantData : Serializable {
    @JsonField(name = ["list"])
    var list =  ArrayList<CategoryMediaRestaurant>()

    @JsonField(name = ["limit"])
    var limit : Int? = 0

    @JsonField(name = ["total_record"])
    var total_record : Int? = 0
}