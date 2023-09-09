package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField

class FeedbackForDevelopersRequest {

    @JsonField(name =["type"])
    var type: String? = null
    @JsonField(name =["error"])
    var error: String? = null

}
