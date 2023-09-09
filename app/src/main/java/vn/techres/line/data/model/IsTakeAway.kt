package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable
import kotlin.collections.ArrayList

@JsonObject
class IsTakeAway : Serializable {

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["price"])
    var price: Int? = 0

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["lat"])
    var lat: String? = ""

    @JsonField(name = ["lng"])
    var lng: String? = ""

    @JsonField(name = ["branchStar"])
    var branchStar: Double? = 0.0

    @JsonField(name = ["point_to_purchase"])
    var point_to_purchase: Int? = 0

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String? = ""

    @JsonField(name = ["branch_name"])
    var branch_name: String? = ""

    @JsonField(name = ["address_full_text"])
    var address_full_text: String? = ""

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["serve_times"])
    var serve_times = ArrayList<ServeTimes>()

    @JsonField(name = ["phone_number"])
    var phone_number: String? = ""

    @JsonField(name = ["branch_image_url"])
    var branch_image_urls: String? = ""

    @JsonField(name = ["branch_business_type_names"])
    var branch_business_type_names = ArrayList<String>()


}