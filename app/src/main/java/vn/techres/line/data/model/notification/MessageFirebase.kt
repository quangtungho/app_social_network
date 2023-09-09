package vn.techres.line.data.model.notification

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class MessageFirebase : Serializable {
    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["type"])
    var type: String? = ""

    @JsonField(name = ["title"])
    var title: String? = ""

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["value"])
    var value: String? = ""

}