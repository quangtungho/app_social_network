package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class SetPasswordRequest : Serializable {
    @JsonField(name=["phone"])
    var phone : String?=null
    @JsonField(name=["verify_code"])
    var verify_code : String?=null
    @JsonField(name=["new_password"])
    var new_password : String?=null
}