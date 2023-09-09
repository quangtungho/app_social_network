package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.Background
import vn.techres.line.data.model.response.BaseResponse

class BackgroundResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<Background>()
}