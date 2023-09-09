package vn.techres.line.data.model.album.params

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.album.request.ImageAlbumRequest
import vn.techres.line.data.model.request.BaseRequest

class ImageAlbumParams: BaseRequest() {
    @JsonField(name = ["params"])
    var params = ImageAlbumRequest()
}