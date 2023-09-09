package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField

class OrderInvoiceResponse : BaseResponse(){

    @JsonField(name = ["data"])
    var data : Int? = 0

}