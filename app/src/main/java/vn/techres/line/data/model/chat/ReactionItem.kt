package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ReactionItem {
    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["number"])
    var number: Int? = 0
}