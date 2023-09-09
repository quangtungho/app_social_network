package vn.techres.line.data.model.chat.data

import java.io.Serializable
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.chat.Pagination

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class MessageData : Serializable {
    @JsonField(name = ["total_records"])
    var total_records : Int? = 0

    @JsonField(name = ["list"])
    var list = ArrayList<MessagesByGroup>()

    @JsonField(name = ["is_oldest_message"])
    var is_oldest_message = false

    @JsonField(name = ["is_latest_message"])
    var is_latest_message = false

    @JsonField(name = ["pagination"])
    var pagination = Pagination("-1","-1")
}