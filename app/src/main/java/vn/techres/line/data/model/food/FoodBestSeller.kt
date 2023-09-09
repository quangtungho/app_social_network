package vn.techres.line.data.model.food

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable
import java.math.BigDecimal

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class FoodBestSeller : Serializable{
    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["price"])
    var price: BigDecimal? = BigDecimal.ZERO

    @JsonField(name = ["avatar"])
    var avatar: Avatar? = null

    @JsonField(name = ["lat"])
    var lat: String? = ""

    @JsonField(name = ["lng"])
    var lng: String? = ""

    @JsonField(name = ["point_to_purchase"])
    var point_to_purchase: Int? = 0

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String? = ""

    @JsonField(name = ["branch_name"])
    var branch_name: String? = ""

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

//    @JsonField(name = ["serve_times"])
//    var serve_times: Int? = 0

    @JsonField(name = ["address_full_text"])
    var address_full_text: String? = ""

    @JsonField(name = ["phone_number"])
    var phone_number: String? = ""

    @JsonField(name = ["branch_image_url"])
    var branch_image_url: String? = ""

    @JsonField(name = ["branchStar"])
    var branchStar: Double? = 0.0

//    @JsonField(name = ["branch_business_type_names"])
//    var branch_business_type_names: String? = ""
}