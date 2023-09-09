package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.contact.Customer
import java.io.Serializable

@JsonObject
class Notifications : Serializable {

    @JsonField(name = ["_id"])
    var _id: String = ""

    @JsonField(name = ["user_id"])
    var user_id: Int = 0

    @JsonField(name = ["customer"])
    var customer = Customer()

    @JsonField(name = ["type"])
    var type: Int? = 0

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["redirect_to"])
    var redirect_to: String = ""

    @JsonField(name = ["action_type"])
    var action_type: Int = 0

    @JsonField(name = ["is_viewed"])
    var is_viewed: Int = 0

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

}