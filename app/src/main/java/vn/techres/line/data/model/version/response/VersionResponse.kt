package vn.techres.line.data.model.version.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.version.Version

class VersionResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = Version()
}