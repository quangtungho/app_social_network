package vn.techres.line.data.model.gift

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.Food
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 1/14/2022
 */

@JsonObject
class Gift  : Serializable {

    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["customer_id"])
    var customer_id: Int? = 0

    @JsonField(name = ["restaurant_id"])
    var restaurant_id: Int? = 0

    @JsonField(name = ["restaurant_brand_id"])
    var restaurant_brand_id: Int? = 0

    @JsonField(name = ["restaurant_gift_id"])
    var restaurant_gift_id: Int? = 0

    @JsonField(name = ["restaurant_gift_object_value"])
    var restaurant_gift_object_value: Int? = 0

    @JsonField(name = ["restaurant_gift_type"])
    var restaurant_gift_type: Int? = 0

    @JsonField(name = ["is_opened"])
    var is_opened: Int? = 0

    @JsonField(name = ["is_expired"])
    var is_expired: Int? = 0

    @JsonField(name = ["is_used"])
    var is_used: Int? = 0

    @JsonField(name = ["restaurant_gift_object_quantity"])
    var restaurant_gift_object_quantity: Double? = 0.0

    @JsonField(name = ["cusotmer_name"])
    var cusotmer_name: String? = ""

    @JsonField(name = ["restaurant_name"])
    var restaurant_name: String? = ""

    @JsonField(name = ["restaurant_brand_name"])
    var restaurant_brand_name: String? = ""

    @JsonField(name = ["restaurant_avatar"])
    var restaurant_avatar: String? = ""

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["description"])
    var description: String? = ""

    @JsonField(name = ["expire_at"])
    var expire_at: String? = ""

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["food"])
    var food = ArrayList<Food>()

    @JsonField(name = ["restaurant_gift_image_url"])
    var restaurant_gift_image_url: String? = ""

    @JsonField(name = ["restaurant_gift_banner_url"])
    var restaurant_gift_banner_url: String? = ""
}