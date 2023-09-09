package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class OptionVote : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["list_user"])
    var list_user: ArrayList<Sender> = ArrayList()
}