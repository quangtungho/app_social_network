package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class ServeTime : Serializable {
    @JsonField(name = ["day_of_week"])
    var day_of_week: Int? = 0

    @JsonField(name = ["day_of_week_name"])
    var day_of_week_name: String? = ""

    @JsonField(name = ["open_time"])
    var open_time: String? = ""

    @JsonField(name = ["close_time"])
    var close_time: String? = ""
}