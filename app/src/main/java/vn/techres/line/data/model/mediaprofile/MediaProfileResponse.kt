package vn.techres.line.data.model.mediaprofile

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.response.BaseResponse

class MediaProfileResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = MediaProfileData()
}