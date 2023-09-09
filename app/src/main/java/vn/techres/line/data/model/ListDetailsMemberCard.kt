package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ListDetailsMemberCard : Serializable {
       @JsonField(name = ["point"])
    var point: Int? = 0

    @JsonField(name = ["note"])
    var note: String? = ""

    @JsonField(name = ["type"])
    var type: Int? = 0

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["branch_name"])
    var branch_name: String? = ""

    @JsonField(name = ["order_id"])
    var order_id: Int? = 0

    @JsonField(name = ["accumulate_point"])
    var accumulate_point: Int? = 0

    @JsonField(name = ["promotion_point"])
    var promotion_point: Int? = 0

    @JsonField(name = ["total_all_point"])
    var total_all_point: Int? = 0

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

}