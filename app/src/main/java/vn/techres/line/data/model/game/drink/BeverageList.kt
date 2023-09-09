package vn.techres.line.data.model.game.drink

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BeverageList: Serializable {
    @JsonField(name = ["avatar"])
    var avatar: String = ""

    @JsonField(name = ["status"])
    var status: Int = 0

    @JsonField(name = ["name"])
    var name: String = ""

    @JsonField(name = ["total_vote"])
    var total_vote: Int = 0

    @JsonField(name = ["code"])
    var code: Int = 0

    @JsonField(name = ["_id"])
    var _id: String = ""

    @JsonField(name = ["article_avatar"])
    var article_avatar = ArticleAvatarDrink()

    @JsonField(name = ["article_content"])
    var article_content = ArticleContentDrink()

    @JsonField(name = ["drink_background"])
    var drink_background = BackgroundDrink()

    @JsonField(name = ["beverage_id"])
    var beverage_id : String = ""
}