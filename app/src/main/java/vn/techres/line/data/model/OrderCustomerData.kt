package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class OrderCustomerData : Serializable {
    @JsonField(name=["list"])
    var list = ArrayList<OrderCustomer>()

    @JsonField(name=["total_record"])
    var total_record : Int? = 0

    @JsonField(name=["limit"])
    var limit : Int? = 0
}