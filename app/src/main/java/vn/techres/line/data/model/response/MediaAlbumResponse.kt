package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.MediaAlbumData

class MediaAlbumResponse: BaseResponse()  {
    @JsonField(name = ["data"])
    var data = MediaAlbumData()
}