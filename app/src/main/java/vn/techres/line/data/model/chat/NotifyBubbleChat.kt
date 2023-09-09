package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class NotifyBubbleChat : Serializable {
    @JsonField(name = ["value"])
    var value : String = ""

    @JsonField(name = ["title"])
    var title : String = ""

    @JsonField(name = ["avatar"])
    var avatar : String = ""

    @JsonField(name = ["full_name"])
    var full_name : String? = ""

    @JsonField(name = ["member_id"])
    var member_id : Int? = 0

    @JsonField(name = ["badgeCount"])
    var badgeCount : String? = ""

    @JsonField(name = ["type_message"])
    var type_message : String? = ""

    @JsonField(name = ["conversation_type"])
    var conversation_type : String? = ""

    @JsonField(name = ["body"])
    var body : String? = ""

    @JsonField(name = ["type"])
    var type : Int? = 0
}