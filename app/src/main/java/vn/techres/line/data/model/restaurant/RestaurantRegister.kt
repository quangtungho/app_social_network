package vn.techres.line.data.model.restaurant

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Logo
import vn.techres.line.data.model.utils.Media
import java.io.Serializable
import java.util.ArrayList

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class RestaurantRegister : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["lat"])
    var lat: String? = ""

    @JsonField(name = ["lng"])
    var lng: String? = ""

    @JsonField(name = ["rating"])
    var rating: Double? = 0.0

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String? = ""

    @JsonField(name = ["image_logo_url"])
    var image_logo_url = Logo()

    @JsonField(name = ["banner_image_url"])
    var banner_image_url=  Media()

    @JsonField(name = ["image_urls"])
    var image_urls = ArrayList<String>()

    @JsonField(name = ["address_full_text"])
    var address_full_text: String? = ""

    @JsonField(name = ["phone_number"])
    var phone_number: String? = ""

    @JsonField(name = ["membership_register_code"])
    var membership_register_code: String? = ""
}