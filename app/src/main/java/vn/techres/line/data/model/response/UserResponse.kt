package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.utils.User

class UserResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data: User? = null
}
