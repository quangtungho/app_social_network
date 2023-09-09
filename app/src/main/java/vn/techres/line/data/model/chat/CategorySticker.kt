package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CategorySticker : Serializable {
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["link_original"])
    var link_original: String? = ""

    @JsonField(name = ["id_category"])
    var id_category: Int? = 0

    @JsonField(name = ["stickers"])
    var stickers: ArrayList<Sticker>? = null
}