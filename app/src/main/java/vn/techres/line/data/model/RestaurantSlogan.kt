package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class RestaurantSlogan : Serializable {
    @JsonField(name = ["id"])
    var id: Int = 0

    @JsonField(name = ["content"])
    var content: String = ""

    @JsonField(name = ["status"])
    var status: Int = 0

    @JsonField(name = ["to_hour"])
    var to_hour: Int = 0

    @JsonField(name = ["from_hour"])
    var from_hour: Int = 0

    @JsonField(name = ["created_at"])
    var created_at: String = ""

    @JsonField(name = ["updated_at"])
    var updated_at: String = ""
}