package vn.techres.line.data.model.chat.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.data.CategoryStickerData
import vn.techres.line.data.model.response.BaseResponse
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CategoryStickerResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = CategoryStickerData()
}