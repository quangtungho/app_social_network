package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class LevelValue : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["point_target"])
    var point_target: Int? = 0

    @JsonField(name = ["point_bonus"])
    var point_bonus: Int? = 0

    @JsonField(name = ["total_point_bonus"])
    var total_point_bonus: Int? = 0
}