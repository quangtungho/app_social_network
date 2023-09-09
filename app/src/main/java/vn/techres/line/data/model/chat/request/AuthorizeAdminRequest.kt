package vn.techres.line.data.model.chat.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class AuthorizeAdminRequest {

    @JsonField(name = ["admin_id"])
    var admin_id : Int? = 0

    @JsonField(name = ["member_id"])
    var member_id : Int? = 0
}