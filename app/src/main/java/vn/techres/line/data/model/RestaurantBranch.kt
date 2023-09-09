package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class
RestaurantBranch : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["average_rate"])
    var average_rate: Double? = 0.0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["logo_url"])
    var logo_url: String? = ""

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["lat"])
    var lat: String? = ""

    @JsonField(name = ["lng"])
    var lng: String? = ""

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String? = ""

    @JsonField(name = ["image_logo_url"])
    var image_logo_url: String? = ""

    @JsonField(name = ["banner_image_url"])
    var banner_image_url: String? = ""

    @JsonField(name = ["address_full_text"])
    var address_full_text: String? = ""

    @JsonField(name = ["phone_number"])
    var phone_number: String? = ""

    @JsonField(name = ["banner"])
    var banner: String? = ""

    @JsonField(name = ["description"])
    var description: String? = ""
}