package vn.techres.line.data.model.album.params

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.request.BaseRequest

class AlbumParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = AlbumRequest("")
}

class AlbumRequest {
    constructor(folderName: String) {
        this.folderName = folderName
    }
    var folderName = ""
}