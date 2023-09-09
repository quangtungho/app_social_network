package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class SuggestFile() : Serializable {
    @JsonField(name = ["file"])
    var file: ArrayList<String>? = null

    @JsonField(name = ["time"])
    var time: String? = ""

    constructor(file: ArrayList<String>?, time: String) : this(){
        this.file = file
        this.time = time
    }
}