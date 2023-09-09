package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField

class CreateAlbumRequest {
    @JsonField(name = ["name"])
    var name: String? = ""
    
    @JsonField(name = ["sort"])
    var sort: Int? = 0
}