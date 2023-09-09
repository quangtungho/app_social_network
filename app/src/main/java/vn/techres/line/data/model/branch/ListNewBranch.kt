package vn.techres.line.data.model.branch

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class ListNewBranch : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

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
}