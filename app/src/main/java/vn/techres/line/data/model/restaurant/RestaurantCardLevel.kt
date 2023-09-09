package vn.techres.line.data.model.restaurant

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class RestaurantCardLevel : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["prerogatives"])
    var prerogatives = ArrayList<Prerogatives>()

    @JsonField(name = ["color_hex_code"])
    var color_hex_code: String? = ""

    @JsonField(name = ["point_to_level_up"])
    var point_to_level_up: String? = ""

    @JsonField(name = ["cashback_to_point_percent"])
    var cashback_to_point_percent: Double? = 0.0

    @JsonField(name = ["total_point"])
    var total_point: Int? = 0

    @JsonField(name = ["rate_got"])
    var rate_got: Double? = 0.0

    @JsonField(name = ["is_my_level"])
    var is_my_level: Int? = 0

    @JsonField(name = ["status"])
    var status: Int? = 0
}