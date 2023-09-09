package vn.techres.data.line.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.utils.Media
import vn.techres.line.data.model.newfeed.Comment
import vn.techres.line.data.model.newfeed.CustomerReview
import vn.techres.line.data.model.newfeed.Link
import vn.techres.line.data.model.restaurant.Restautant

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class PostReview {


    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["branch_review_id"])
    var branch_review_id: Int? = 0

    @JsonField(name = ["branch_review_status"])
    var branch_review_status: Int? = 0

    @JsonField(name = ["title"])
    var title: String? = ""

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["service_rate"])
    var service_rate: Float? = 0.0f

    @JsonField(name = ["food_rate"])
    var food_rate: Float? = 0.0f

    @JsonField(name = ["price_rate"])
    var price_rate: Float? = 0.0f

    @JsonField(name = ["space_rate"])
    var space_rate: Float? = 0.0f

    @JsonField(name = ["hygiene_rate"])
    var hygiene_rate: Float? = 0.0f

    @JsonField(name = ["rate"])
    var rate: Float? = 0.0f

    @JsonField(name = ["media_contents"])
    var media_contents = ArrayList<Media>()

    @JsonField(name = ["url"])
    var url: String? = ""

    @JsonField(name = ["url_json_content"])
    var url_json_content = Link()

    @JsonField(name = ["customer"])
    var customer = CustomerReview()

    @JsonField(name = ["comments"])
    var comments = ArrayList<Comment>()

    @JsonField(name = ["type"])
    var type: Int? = 0

    @JsonField(name = ["view_count"])
    var view_count: Int? = 0

    @JsonField(name = ["comment_count"])
    var comment_count: Int? = 0

    @JsonField(name = ["restaurant"])
    var restaurant = Restautant()

    @JsonField(name = ["branch"])
    var branch = Branch()

    @JsonField(name = ["my_reaction_id"])
    var my_reaction_id: Int? = 0

    @JsonField(name = ["reaction_one_count"])
    var reaction_one_count: Int? = 0

    @JsonField(name = ["reaction_two_count"])
    var reaction_two_count: Int? = 0

    @JsonField(name = ["reaction_three_count"])
    var reaction_three_count: Int? = 0

    @JsonField(name = ["reaction_four_count"])
    var reaction_four_count: Int? = 0

    @JsonField(name = ["reaction_five_count"])
    var reaction_five_count: Int? = 0

    @JsonField(name = ["reaction_six_count"])
    var reaction_six_count: Int? = 0

    @JsonField(name = ["alo_point_bonus_level_id"])
    var alo_point_bonus_level_id: Int? = 0

    @JsonField(name = ["reaction_point"])
    var reaction_point: Int? = 0

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["updated_at"])
    var updated_at: String? = ""

    @JsonField(name = ["loading"])
    var loading: Int? = 0

    @JsonField(name = ["status_reload"])
    var status_reload: String? = ""


    //    @JsonField(name = ["media_contents_three_image"])
//    var media_contents_three_image = ArrayList<Media>()
//
//    @JsonField(name = ["reaction_id"])
//    var reaction_id: Int? = 0
//
//    @JsonField(name = ["number_reactions"])
//    var number_reactions: Int? = 0

    //    @JsonField(name = ["secondYtb"])
//    var secondYtb: Float = 0f
    constructor(_id: String?) {
        this._id = _id
    }

    constructor()

}