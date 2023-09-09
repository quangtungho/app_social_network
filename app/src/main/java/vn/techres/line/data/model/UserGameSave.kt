package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class UserGameSave : Serializable {

    @JsonField(name = ["_id"])
    var _id : String? = ""

    @JsonField(name = ["access_token"])
    var access_token : String? = ""

    @JsonField(name = ["address"])
    var address : String? = ""

    @JsonField(name = ["address_full_text"])
    var address_full_text : String? = ""

    @JsonField(name = ["avatar"])
    var avatar : String? = ""

    @JsonField(name = ["birthday"])
    var birthday : String? = ""

    @JsonField(name = ["branch_id"])
    var branch_id : Int? = 0

    @JsonField(name = ["branch_name"])
    var branch_name : String? = ""

    @JsonField(name = ["city_id"])
    var city_id : Int? = 0

    @JsonField(name = ["company_address"])
    var company_address : String? = ""

    @JsonField(name = ["company_name"])
    var company_name : String? = ""

    @JsonField(name = ["company_phone"])
    var company_phone : String? = ""

    @JsonField(name = ["company_tax_number"])
    var company_tax_number : String? = ""

    @JsonField(name = ["device_uid"])
    var device_uid : String? = ""

    @JsonField(name = ["district_id"])
    var district_id : Int? = 0

    @JsonField(name = ["email"])
    var email : String? = ""

    @JsonField(name = ["employeeRoleDescription"])
    var employeeRoleDescription : String? = ""

    @JsonField(name = ["employeeRoleId"])
    var employeeRoleId : String? = ""

    @JsonField(name = ["employeeRoleName"])
    var employeeRoleName : String? = ""

    @JsonField(name = ["expires_in"])
    var expires_in : String? = ""

    @JsonField(name = ["full_name"])
    var full_name : String? = ""

    @JsonField(name = ["gender"])
    var gender : Int? = 0

    @JsonField(name = ["id"])
    var id : Int? = 0

    @JsonField(name = ["is_checked"])
    var is_checked : Int? = 0

    @JsonField(name = ["is_enable_change_password"])
    var is_enable_change_password : Int? = 0

    @JsonField(name = ["name"])
    var name : String? = ""

    @JsonField(name = ["nodeAccessToken"])
    var nodeAccessToken : String? = ""

    @JsonField(name = ["password"])
    var password : String? = ""

    @JsonField(name = ["phone"])
    var phone : String? = ""

    @JsonField(name = ["refresh_token"])
    var refresh_token : String? = ""

    @JsonField(name = ["restaurant_id"])
    var restaurant_id : Int? = 0

    @JsonField(name = ["restaurant_name"])
    var restaurant_name : String? = ""

    @JsonField(name = ["status"])
    var status : Int? = 0

    @JsonField(name = ["street_name"])
    var street_name : String? = ""

    @JsonField(name = ["token_type"])
    var token_type : String? = ""

    @JsonField(name = ["user_id"])
    var user_id : Int? = 0

    @JsonField(name = ["username"])
    var username : String? = ""

    @JsonField(name = ["ward_id"])
    var ward_id : Int? = 0

    @JsonField(name = ["working_from"])
    var working_from : String? = ""
}