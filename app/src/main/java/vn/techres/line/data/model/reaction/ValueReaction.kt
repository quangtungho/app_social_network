package vn.techres.line.data.model.reaction

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class ValueReaction  : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["icon"])
    var icon: String? = ""

    @JsonField(name = ["value"])
    var value: Int? = 0

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["updated_at"])
    var updated_at: String? = ""
}