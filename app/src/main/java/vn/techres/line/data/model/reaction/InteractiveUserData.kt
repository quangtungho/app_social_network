package vn.techres.line.data.model.reaction

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class InteractiveUserData : Serializable {
    @JsonField(name = ["list"])
    var list = ArrayList<InteractiveUser>()

    @JsonField(name = ["total_record"])
    var total_record : Int? = 0
}