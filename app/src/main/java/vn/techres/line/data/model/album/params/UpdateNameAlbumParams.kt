package vn.techres.line.data.model.album.params

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.album.request.UpdateNameAlbumRequest
import vn.techres.line.data.model.request.BaseRequest

class UpdateNameAlbumParams: BaseRequest() {
    @JsonField(name = ["params"])
    var params = UpdateNameAlbumRequest()
}