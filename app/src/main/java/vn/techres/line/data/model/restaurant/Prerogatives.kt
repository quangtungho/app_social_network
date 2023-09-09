package vn.techres.line.data.model.restaurant

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class Prerogatives : Serializable {
    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["description"])
    var description: String? = ""
}