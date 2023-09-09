package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class ReVerifyCodePhoneRequest: Serializable {
    @JsonField(name=["phone"])
    var phone: String?=null

    @JsonField(name=["user_uid"])
    var user_uid: String?=null

    @JsonField(name=["verify_code"])
    var verify_code: String? = ""

    @JsonField(name=["new_password"])
    var new_password: String?=null

    @JsonField(name=["password"])
    var password: String?=null

    @JsonField(name=["type_user"])
    var type_user: Int?= 1

    @JsonField(name=["birthday"])
    var birthday: String?=null

    @JsonField(name=["name"])
    var name: String?=null

    @JsonField(name=["node_access_token"])
    var node_access_token: String?=null

}