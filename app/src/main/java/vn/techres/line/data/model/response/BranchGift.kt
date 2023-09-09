package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField

class BranchGift {
    @JsonField(name = ["id"])
    var id: Int = 0

    @JsonField(name = ["status"])
    var status: Int = 0

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int = 0

    @JsonField(name = ["name"])
    var name: String = ""

    @JsonField(name = ["banner"])
    var banner: String = ""

    @JsonField(name = ["description"])
    var description: String = ""

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String = ""

    @JsonField(name = ["logo_url"])
    var logo_url: String = ""

    @JsonField(name = ["created_at"])
    var created_at: String = ""


}