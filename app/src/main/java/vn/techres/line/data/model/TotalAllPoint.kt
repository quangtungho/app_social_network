package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class TotalAllPoint: Serializable {
    @JsonField(name = ["id"])
    var id : Int? = 0

    @JsonField(name = ["point"])
    var point : Int? = 0

    @JsonField(name = ["accumulate_point"])
    var accumulate_point : Int? = 0

    @JsonField(name = ["promotion_point"])
    var promotion_point : Int? = 0

    @JsonField(name = ["total_all_point"])
    var total_all_point : Int? = 0
}