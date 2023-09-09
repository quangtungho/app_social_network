package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable
@JsonObject
class BarCodeRequest: Serializable {

    @JsonField(name=["membership_code"])
    var membership_code: String?=null
}