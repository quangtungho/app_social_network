package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField

class VerifyCodePhoneRequest {
    @JsonField(name = ["phone"])
    var phone: String?=null
    @JsonField(name = ["verify_code"])
    var verify_code: String?=null

}