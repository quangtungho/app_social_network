package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.newfeed.Link
import vn.techres.line.data.model.utils.Media

@JsonObject
class ReviewBranchRequest {
    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["title"])
    var title: String? = ""

    @JsonField(name = ["rate"])
    var rate: Double? = 0.0

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["image_urls"])
    var media_contents = ArrayList<Media>()

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["service_rate"])
    var service_rate: Float? = 1.0f

    @JsonField(name = ["food_rate"])
    var food_rate: Float? = 1.0f

    @JsonField(name = ["price_rate"])
    var price_rate: Float? = 1.0f

    @JsonField(name = ["space_rate"])
    var space_rate: Float? = 1.0f

    @JsonField(name = ["hygiene_rate"])
    var hygiene_rate: Float? = 1.0f

    @JsonField(name = ["branch_review_status"])
    var branch_review_status: Int? = 0

    @JsonField(name = ["url"])
    var url: String? = ""


    @JsonField(name = ["url_json_content"])
    var url_json_content = Link()
}

