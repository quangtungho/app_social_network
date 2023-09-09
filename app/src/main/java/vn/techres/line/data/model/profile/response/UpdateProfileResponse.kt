package vn.techres.line.data.model.profile.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.User

class UpdateProfileResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data = User()
}