package vn.techres.line.data.model.branch

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.RateDetail
import vn.techres.line.data.model.utils.Avatar
import vn.techres.line.data.model.utils.Logo
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Branch : Serializable {
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

    @JsonField(name = ["logo_image_url"])
    var logo_image_url = Logo()

    @JsonField(name = ["image_logo_url"])
    var image_logo_url = Avatar()

    @JsonField(name = ["banner_image_url"])
    var banner_image_url = Avatar()

    @JsonField(name = ["image_urls"])
    var image_urls = ArrayList<String>()

    @JsonField(name = ["address_full_text"])
    var address_full_text: String? = ""

    @JsonField(name = ["phone_number"])
    var phone_number: String? = ""

    @JsonField(name = ["is_check"])
    var is_check: Boolean? = false

    @JsonField(name = ["rating"])
    var rating: Float? = 0.0f

    @JsonField(name = ["rate"])
    var rate: Float? = 0.0f

    @JsonField(name = ["rate_count"])
    var rate_count: Int? = 0

    @JsonField(name = ["rate_detail"])
    var rate_detail = RateDetail()
}