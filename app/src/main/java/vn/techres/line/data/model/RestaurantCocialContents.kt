package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class RestaurantCocialContents {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["title"])
    var title: String? = ""

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["branch_name"])
    var branch_name: String? = ""

    @JsonField(name = ["branch_address"])
    var branch_address: String? = ""

    @JsonField(name = ["branch_phone"])
    var branch_phone: String? = ""

    @JsonField(name = ["image_urls"])
    var image_urls = ArrayList<String>()

    @JsonField(name = ["comment_count"])
    var comment_count: Int? = 0

    @JsonField(name = ["like_count"])
    var like_count: Int? = 0

    @JsonField(name = ["liked"])
    var liked: Int? = 0

    @JsonField(name = ["view_count"])
    var view_count: Int? = 0

    @JsonField(name = ["shared_count"])
    var shared_count: Int? = 0

    @JsonField(name = ["social_id"])
    var social_id: String? = "0"

    @JsonField(name = ["social_type"])
    var social_type: Int? = 0

}