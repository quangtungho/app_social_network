package vn.techres.line.data.model.album.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.album.FolderAlbum
import vn.techres.line.data.model.response.BaseResponse

class AlbumResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = AlbumData()
}
class AlbumData {
    var total_record = 0
    var list = ArrayList<FolderAlbum>()
}