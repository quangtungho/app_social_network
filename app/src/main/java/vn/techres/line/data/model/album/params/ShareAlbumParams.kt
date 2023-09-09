package vn.techres.line.data.model.album.params

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.album.request.ShareAlbumRequest
import vn.techres.line.data.model.request.BaseRequest

class ShareAlbumParams : BaseRequest() {
    @JsonField(name = ["params"])
    var params = ShareAlbumRequest()
}