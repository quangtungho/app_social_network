package vn.techres.line.data.model.card

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class DenominationCard : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["amount"])
    var amount: Float? = 0F

    @JsonField(name = ["is_check"])
    var is_check: Boolean = false
}