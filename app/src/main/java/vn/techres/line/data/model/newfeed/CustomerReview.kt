package vn.techres.line.data.model.newfeed

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Avatar
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CustomerReview : Serializable {
    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["avatar"])
    var avatar = Avatar()

    @JsonField(name = ["is_display_nick_name"])
    var is_display_nick_name: Int? = 0

    @JsonField(name = ["nick_name"])
    var nick_name: String? = ""

}