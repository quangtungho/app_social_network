package vn.techres.line.data.model.game.drink

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ArticleAvatarDrink : Serializable {
    @JsonField(name = ["cans"])
    var cans: String = ""

    @JsonField(name = ["log"])
    var log: String = ""

    @JsonField(name = ["container"])
    var container: String = ""
}