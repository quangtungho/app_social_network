package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class EventBusCallVideo() : Serializable {
    @JsonField(name = ["status"])
    var status: Boolean? = false
    constructor(status: Boolean) : this(){
        this.status = status
    }
}