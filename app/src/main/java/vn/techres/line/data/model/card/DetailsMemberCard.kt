package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class DetailsMemberCard : Serializable {
    @JsonField(name = ["limit"])
    var limit: Int? = 0

    @JsonField(name = ["list"])
    var list = ArrayList<ListDetailsMemberCard>()

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String? = ""

    @JsonField(name = ["total_record"])
    var total_record: Int? = 0

}