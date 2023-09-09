package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ListPointHistory :Serializable{
    @JsonField(name=["type"])
    var type : Int? = 0
    @JsonField(name=["restaurant_name"])
    var restaurant_name : String? = ""
    @JsonField(name=["branch_name"])
    var branch_name : String? = ""
    @JsonField(name=["total_point"])
    var total_point : Int? = 0
    @JsonField(name=["order_id"])
    var order_id : Int? = 0
    @JsonField(name=["order_amount"])
    var order_amount : Float? = 0.0F
    @JsonField(name=["created_at"])
    var created_at : String? = ""
    @JsonField(name=["expire_at"])
    var expire_at : String? = ""


}