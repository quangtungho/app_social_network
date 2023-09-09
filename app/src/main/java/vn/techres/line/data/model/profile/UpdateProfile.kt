package vn.techres.line.data.model.profile

import com.bluelinelabs.logansquare.annotation.JsonField

import java.io.Serializable

class UpdateProfile : Serializable{
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["first_name"])
    var first_name: String? = ""

    @JsonField(name = ["last_name"])
    var last_name: String? = ""

    @JsonField(name = ["email"])
    var email: String? = ""

    @JsonField(name = ["phone"])
    var phone: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["gender"])
    var gender: Int? = 0

    @JsonField(name = ["birthday"])
    var birthday: String? = ""

    @JsonField(name = ["address_full_text"])
    var address_full_text: String? = ""

    @JsonField(name = ["street_name"])
    var street_name: String? = ""

    @JsonField(name = ["city_id"])
    var city_id: Int? = 0

    @JsonField(name = ["district_id"])
    var district_id: Int? = 0

    @JsonField(name = ["ward_id"])
    var ward_id: Int? = 0

    @JsonField(name = ["cover_urls"])
    var cover_urls = ArrayList<String>()

    @JsonField(name = ["is_display_nick_name"])
    var is_display_nick_name: Int? = 0

    @JsonField(name = ["nick_name"])
    var nick_name: String? = ""
}