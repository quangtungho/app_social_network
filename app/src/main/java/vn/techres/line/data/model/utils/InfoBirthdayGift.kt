package vn.techres.line.data.model.utils


import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class InfoBirthdayGift : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["title"])
    var title: String? = ""

    @JsonField(name = ["message"])
    var message: String? = ""

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["gift"])
    var gift = ArrayList<String>()

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String? = ""

    @JsonField(name = ["branch_name"])
    var branch_name: String? = ""

    @JsonField(name = ["image_url"])
    var image_url: String? = ""

    @JsonField(name = ["icon_image_url"])
    var icon_image_url: String? = ""

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

}