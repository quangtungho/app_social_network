package vn.techres.line.data.model.chat.data

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.MessagesByGroup
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class PinnedData : Serializable {
    @JsonField(name = ["list"])
    var list = ArrayList<MessagesByGroup>()

    @JsonField(name = ["total_record"])
    var total_record : Int? = 0
}