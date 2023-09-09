package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.CreatePost


@JsonIgnoreProperties(ignoreUnknown = true)
class CreatePostResponse: BaseResponse() {

    @JsonField(name = ["data"])
    var data : CreatePost? = null
}