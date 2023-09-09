package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.bill.BillTable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BillTableResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data = BillTable()
}