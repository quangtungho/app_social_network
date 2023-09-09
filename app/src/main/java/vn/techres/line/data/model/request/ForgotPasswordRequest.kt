package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class ForgotPasswordRequest : Serializable {
    @JsonField(name=["phone"])
    var phone:String?= ""
}