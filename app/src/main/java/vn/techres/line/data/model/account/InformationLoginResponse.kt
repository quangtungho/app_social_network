package vn.techres.line.data.model.account

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.response.BaseResponse

class InformationLoginResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data = InformationLogin()
}