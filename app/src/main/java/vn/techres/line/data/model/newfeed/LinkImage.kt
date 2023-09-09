package vn.techres.line.data.model.newfeed

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class LinkImage : Serializable {

    @JsonField(name = ["name_file"])
    var name_file: String = ""

    @JsonField(name = ["type"])
    var type: Int = 0

    @JsonField(name = ["link_original"])
    var link_original: String = ""

    @JsonField(name = ["link_thumbs"])
    var link_thumb: String = ""

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int = 0

    @JsonField(name = ["branch_id"])
    var branch_id: Int = 0

    @JsonField(name = ["user_id"])
    var user_id: Int = 0

    @JsonField(name = ["type_file"])
    var type_file: String = ""

    @JsonField(name = ["created_at"])
    var created_at: String = ""

    @JsonField(name = ["updated_at"])
    var updated_at: String = ""

    @JsonField(name = ["_id"])
    var _id: String = ""

    @JsonField(name = ["__v"])
    var __v: Int = 0

    @JsonField(name = ["width"])
    var width: Int = 0

    @JsonField(name = ["height"])
    var height: Int = 0

    @JsonField(name = ["ratio"])
    var ratio: Float = 0f
}