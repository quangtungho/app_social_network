package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.User

@JsonIgnoreProperties(ignoreUnknown = true)
class VerifyResetPasswordResponse  : BaseResponse(){
    @JsonField(name=["data"])
    var data: User?=null
}