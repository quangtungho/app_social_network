package vn.techres.line.data.model.comment

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.Tags
import vn.techres.line.data.model.utils.Avatar
import vn.techres.line.data.model.utils.Url
import java.io.Serializable

@JsonObject
class CommentProfile: Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["type"])
    var type: Int? = 0

    @JsonField(name = ["comments"])
    var comments = ArrayList<CommentProfile>()

    @JsonField(name = ["customer_id"])
    var customer_id: Int? = 0

    @JsonField(name = ["customer_name"])
    var customer_name: String? = ""

    @JsonField(name = ["customer_avatar"])
    var customer_avatar = Avatar()

    @JsonField(name = ["reply_count"])
    var reply_count: Int? = 0

    @JsonField(name = ["customer_tags"])
    var customer_tags = ArrayList<Tags>()

    @JsonField(name = ["image_urls"])
    var image_urls = ArrayList<Url>()

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["customer_like_ids"])
    var customer_like_ids = ArrayList<Int>()

    @JsonField(name = ["my_reaction_id"])
    var my_reaction_id: Int? = 0

    @JsonField(name = ["total_commnent"])
    var total_commnent: Int? = 0

    @JsonField(name = ["total_commnent_dislay_outside"])
    var total_commnent_dislay_outside: Int? = 0

    @JsonField(name = ["sticker"])
    var sticker: String? = ""

}