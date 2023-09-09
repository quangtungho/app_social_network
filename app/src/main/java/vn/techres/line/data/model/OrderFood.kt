package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class OrderFood : Serializable {
    @JsonField(name = ["name"])
    var name: String? = null

    @JsonField(name = ["quantity"])
    var quantity: String? = null

    @JsonField(name = ["price"])
    var price: String? = null

    @JsonField(name = ["food_id"])
    var food_id: Int? = null

    @JsonField(name = ["total_amount"])
    var total_amount: String? = null
}