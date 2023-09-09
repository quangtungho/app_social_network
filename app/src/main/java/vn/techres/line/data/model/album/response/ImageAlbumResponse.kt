package vn.techres.line.data.model.album.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.album.ImageFolder
import vn.techres.line.data.model.response.BaseResponse

class ImageAlbumResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ImageFolderData()
}
class ImageFolderData {
    var list = ArrayList<ImageFolder>()
    var total_records = 0
}