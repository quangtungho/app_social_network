package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */

@JsonObject
class CategoryMediaRestaurant : Serializable {
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["folder_name"])
    var folder_name: String? = ""

    @JsonField(name = ["folder_path"])
    var folder_path: String? = ""

    @JsonField(name = ["category_id"])
    var category_id: String? = ""

    @JsonField(name = ["link"])
    var link: String? = ""

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["updated_at"])
    var updated_at: String? = ""

    @JsonField(name = ["user_id"])
    var user_id: Int? = 0

    @JsonField(name = ["__v"])
    var __v: Int? = 0

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["branch_id"])
    var branch_id: Int? = 0

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["is_public"])
    var is_public: Int? = 0

}