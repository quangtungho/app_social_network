package vn.techres.line.data.model.newfeed

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class DetailReactionReview: Serializable {
    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["icon"])
    var icon: String? = ""

    @JsonField(name = ["customer_id"])
    var customer_id: Int? = 0

    @JsonField(name = ["reaction_id"])
    var reaction_id: Int? = 0

    @JsonField(name = ["customer_name"])
    var customer_name: String? = ""

    @JsonField(name = ["customer_avatar"])
    var customer_avatar: String? = ""

    @JsonField(name = ["customer_phone"])
    var customer_phone: String? = ""

    @JsonField(name = ["status"])
    var status: Int? = 0
}