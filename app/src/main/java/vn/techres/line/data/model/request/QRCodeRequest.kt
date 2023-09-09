package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class QRCodeRequest:Serializable {
    @JsonField(name=["id"])
    var id:Int?=0
}