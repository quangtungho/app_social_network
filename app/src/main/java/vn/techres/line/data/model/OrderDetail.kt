package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.math.BigDecimal

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class OrderDetail : Serializable {

    @JsonField(name=["id"])
    var id : Int? = 0

    @JsonField(name=["food_name"])
    var food_name : String? = ""

    @JsonField(name=["order_status"])
    var order_status : Int? = 0

    @JsonField(name=["order_status_name"])
    var order_status_name : String? = ""

    @JsonField(name=["price"])
    var price : BigDecimal? = BigDecimal.ZERO

    @JsonField(name=["total_amount"])
    var total_amount : BigDecimal? = BigDecimal.ZERO

    @JsonField(name=["quantity"])
    var quantity : Double? = 0.0

    @JsonField(name=["is_gift"])
    var is_gift : Int? = 0

    @JsonField(name=["is_purchase_by_point"])
    var is_purchase_by_point : Int? = 0

    @JsonField(name=["total_point_to_purchase"])
    var total_point_to_purchase : Int? = 0

}