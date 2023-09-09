package vn.techres.line.data.model.album.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.album.FolderAlbum
import vn.techres.line.data.model.response.BaseResponse

class CreateAlbumResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = FolderAlbum()
}