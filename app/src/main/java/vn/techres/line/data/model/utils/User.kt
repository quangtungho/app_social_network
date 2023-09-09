package vn.techres.line.data.model.utils
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonProperty
import vn.techres.line.data.model.chat.Group

import java.io.Serializable

/**
 * Created by kelvin on 27/06/19.
 */
@JsonObject
class User : Serializable {
    ////GETTER


    @JsonProperty("id")
    var id: Int = 0

    @JsonProperty("user_id")
    var user_id: Int = 0

    @JsonProperty("_id")
    var _id: String = ""

    @JsonProperty("name")
    var name :String? = ""

    @JsonProperty("first_name")
    var first_name :String = ""

    @JsonProperty("last_name")
    var last_name :String = ""

    @JsonProperty("full_name")
    var full_name :String = ""

    @JsonProperty("nick_name")
    var nick_name :String = ""

    @JsonProperty("email")
    var email :String = ""

    @JsonProperty("phone")
    var phone :String = ""

    @JsonProperty("avatar")
    var avatar :String = ""

    @JsonProperty("avatar_three_image")
    var avatar_three_image = Avatar()

    @JsonField(name = ["cover_urls"])
    var cover_urls = ArrayList<String>()

    @JsonProperty("birthday")
    var birthday :String = ""

    @JsonProperty("address_full_text")
    var address_full_text :String = ""

    @JsonProperty("street_name")
    var street_name :String = ""

    @JsonProperty("city_id")
    var city_id :Int = 0

    @JsonProperty("district_id")
    var district_id :Int = 0

    @JsonProperty("ward_id")
    var ward_id :Int = 0

    @JsonProperty("company_name")
    var company_name :String = ""

    @JsonProperty("company_tax_number")
    var company_tax_number :String = ""

    @JsonProperty("company_phone")
    var company_phone :String = ""

    @JsonProperty("company_address")
    var company_address :String = ""

    @JsonProperty("permissions")
    var permissions: List<String>? = null

    @JsonProperty("username")
    var username :String = ""


    @JsonProperty("password")
    var password :String = ""

    @JsonProperty("employee_role_id")
    var employeeRoleId :String = ""

    @JsonProperty("employee_role_name")
    var employeeRoleName :String = ""

    @JsonProperty("employee_role_description")
    var employeeRoleDescription :String = ""

    @JsonProperty("access_token")
    var access_token : String = ""

    @JsonProperty("refresh_token")
    var refresh_token :String = ""

    @JsonProperty("device_uid")
    var device_uid :String = ""

    @JsonProperty("jwt_token")
    var jwt_token : String = ""

    @JsonProperty("node_access_token")
    var nodeAccessToken : String = ""

    @JsonProperty("node_refresh_token")
    var node_refresh_token :String = ""

    @JsonProperty("token_type")
    var token_type :String = ""

    @JsonProperty("restaurant_id")
    var restaurant_id :Int? = 0

    @JsonProperty("restaurant_name")
    var restaurant_name :String = ""

    @JsonProperty("branch_id")
    var branch_id :Int = 0

    @JsonProperty("branch_name")
    var branch_name :String = ""

    @JsonProperty("expires_in")
    var expires_in :String = ""

    @JsonProperty("gender")
    var gender :Int = 0

    @JsonProperty("is_enable_change_password")
    var is_enable_change_password :Int = 0


    @JsonProperty("address")
    var address :String = ""

    @JsonProperty("working_from")
    var working_from :String = ""

    @JsonProperty("token_firebase")
    var token_firebase :String = ""

    @JsonProperty("status")
    var status :Int = 0

    @JsonField(name = ["is_display_nick_name"])
    var is_display_nick_name: Int? = 0

    @JsonField(name = ["city_name"])
    var city_name: String? = ""

    @JsonField(name = ["district_name"])
    var district_name: String? = ""

    @JsonField(name = ["ward_name"])
    var ward_name: String? = ""

    @JsonField(name = ["group"])
    var group : Group? = null

    @JsonProperty("is_checked")
    var is_checked = 0 // 1: checked, 0: uncheck : Only local

    @JsonProperty("is_allow_advert")
    var is_allow_advert = 0 // 0: unregistered, 1: Registered, 2: Waiting

}
