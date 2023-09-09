package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class RestaurantSocialContentsData: Serializable {
    @JsonField(name = ["limit"])
    var limit: Int? = 0

    @JsonField(name = ["list"])
    var list =  ArrayList<RestaurantSocialContents>()

    @JsonField(name = ["total_record"])
    var total_record: Int? = 0
}