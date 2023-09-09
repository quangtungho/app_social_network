package vn.techres.line.data.model.branch

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.RateDetail
import vn.techres.line.data.model.ServeTime
import vn.techres.line.data.model.utils.Logo
import vn.techres.line.data.model.utils.Url
import java.io.Serializable
import java.util.*


@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BranchDetail : Serializable {

    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["lat"])
    var lat: String? = ""

    @JsonField(name = ["lng"])
    var lng: String? = ""

    @JsonField(name = ["star"])
    var star: Double? = 0.0

    @JsonField(name = ["menu"])
    var menu: String? = ""

    @JsonField(name = ["isHaveChildConner"])
    var isHaveChildConner: Int? = 0

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["phone_number"])
    var phone_number: String? = ""

    @JsonField(name = ["address_full_text"])
    var address_full_text: String? = ""

    @JsonField(name = ["average_price"])
    var average_price: Double? = 0.0

    @JsonField(name = ["image_logo_url"])
    var image_logo_url = Logo()

    @JsonField(name = ["banner_image_url"])
    var banner_image_url = Url()

   @JsonField(name = ["image_urls"])
   var image_urls = ArrayList<String>()

    @JsonField(name = ["open_time"])
    var open_time: String? = ""

    @JsonField(name = ["close_time"])
    var close_time: String? = ""

    @JsonField(name = ["working_day"])
    var working_day: String? = ""

    @JsonField(name = ["wifi_name"])
    var wifi_name: String? = ""

    @JsonField(name = ["wifi_password"])
    var wifi_password: String? = ""

    @JsonField(name = ["total_review"])
    var total_review: Int? = 0

    @JsonField(name = ["facebook_page"])
    var facebook_page: String? = ""

    @JsonField(name = ["website"])
    var website: String? = ""

    @JsonField(name = ["is_free_parking"])
    var is_free_parking: Int? = 0

    @JsonField(name = ["is_have_wifi"])
    var is_have_wifi: Int? = 0

    @JsonField(name = ["is_have_air_conditioner"])
    var is_have_air_conditioner: Int? = 0

    @JsonField(name = ["is_have_booking_online"])
    var is_have_booking_online: Int? = 0

    @JsonField(name = ["is_have_order_food_online"])
    var is_have_order_food_online: Int? = 0

    @JsonField(name = ["is_have_shipping"])
    var is_have_shipping: Int? = 0

    @JsonField(name = ["is_have_car_parking"])
    var is_have_car_parking: Int? = 0

    @JsonField(name = ["is_have_private_room"])
    var is_have_private_room: Int? = 0

    @JsonField(name = ["is_have_outdoor"])
    var is_have_outdoor: Int? = 0

    @JsonField(name = ["is_have_child_corner"])
    var is_have_child_corner: Int? = 0

    @JsonField(name = ["is_have_live_music"])
    var is_have_live_music: Int? = 0

    @JsonField(name = ["is_have_karaoke"])
    var is_have_karaoke: Int? = 0

    @JsonField(name = ["is_have_invoice"])
    var is_have_invoice: Int? = 0

    @JsonField(name = ["is_have_card_payment"])
    var is_have_card_payment: Int? = 0

    @JsonField(name = ["other_informations"])
    var other_informations: String? = ""

    @JsonField(name = ["branch_business_types"])
    var branch_business_types = ArrayList<String>()

    @JsonField(name = ["rating"])
    var rating = RateDetail()

    @JsonField(name = ["serve_time"])
    var serve_time = ArrayList<ServeTime>()

}