package vn.techres.line.data.model.profile.request

import com.bluelinelabs.logansquare.annotation.JsonField

class UpdateProfileRequest {
    @JsonField(name = ["first_name"])
    var first_name: String? = ""

    @JsonField(name = ["last_name"])
    var last_name: String? = ""

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["customer_address"])
    var customer_address: String? = ""

    @JsonField(name = ["email"])
    var email: String? = ""

    @JsonField(name = ["birthday"])
    var birthday: String? = ""

    @JsonField(name = ["gender"])
    var gender: Int? = 0

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["city_id"])
    var city_id: String? = ""

    @JsonField(name = ["city_name"])
    var city_name: String? = ""

    @JsonField(name = ["district_id"])
    var district_id: String? = ""

    @JsonField(name = ["district_name"])
    var district_name: String? = ""

    @JsonField(name = ["ward_id"])
    var ward_id: String? = ""

    @JsonField(name = ["ward_name"])
    var ward_name: String? = ""

    @JsonField(name = ["street_name"])
    var street_name: String? = ""

    @JsonField(name = ["phone_number"])
    var phone_number: String? = ""

    @JsonField(name = ["cover_urls"])
    var cover_urls = ArrayList<String>()

    @JsonField(name = ["chat_token"])
    var chat_token: String? = ""

    @JsonField(name = ["is_display_nick_name"])
    var is_display_nick_name: Int? = 0

    @JsonField(name = ["nick_name"])
    var nick_name: String? = ""

    @JsonField(name = ["node_access_token"])
    var node_access_token: String? = ""
}