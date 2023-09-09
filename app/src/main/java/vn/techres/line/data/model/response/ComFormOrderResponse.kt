package vn.techres.line.data.model.response
import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.cart.ComFormData

class ComFormOrderResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data = ComFormData()
}