package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.branch.BranchInfor

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class
NewFeed{
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["title"])
    var title: String? = ""

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["customer_id"])
    var customer_id: Int = 0

    @JsonField(name = ["customer_name"])
    var customer_name: String? = ""

    @JsonField(name = ["customer_avatar"])
    var customer_avatar: String? = ""

    @JsonField(name = ["branch_infor"])
    var branch_infor = BranchInfor()

    @JsonField(name = ["like_count"])
    var like_count: Int? = 0

    @JsonField(name = ["comment_count"])
    var comment_count: Int? = 0

    @JsonField(name = ["customer_tags"])
    var customer_tags = ArrayList<Tags>()

    @JsonField(name = ["customer_likes_ids"])
    var customer_likes_ids = ArrayList<Int>()

    @JsonField(name = ["image_urls"])
    var image_urls = ArrayList<String>()

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["isSelected"])
    var isSelected : Int = 0

    @JsonField(name = ["isSaveBranch"])
    var isSaveBranch : Boolean? = false
}
