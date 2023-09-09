package vn.techres.line.data.model.game.drink

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class ArticleContentDrink : Serializable {
    @JsonField(name = ["capacity"])
    var capacity: Int = 0

    @JsonField(name = ["weight"])
    var weight: Int = 0

    @JsonField(name = ["container"])
    var container: Int = 0
}