package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class EventBusSMS() : Serializable {
    @JsonField(name = ["message"])
    var message : String? = ""
    constructor(message: String?) : this(){
        this.message = message
    }
}