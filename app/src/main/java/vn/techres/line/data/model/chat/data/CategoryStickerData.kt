package vn.techres.line.data.model.chat.data

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.chat.CategorySticker
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CategoryStickerData : Serializable {
    @JsonField(name = ["list_category"])
    var list_category = ArrayList<CategorySticker>()

    @JsonField(name = ["version"])
    var version : Int? = 0
}