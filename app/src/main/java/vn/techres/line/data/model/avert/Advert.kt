package vn.techres.line.data.model.avert

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.utils.Url
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Advert : Serializable {

    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["url"])
    var url: String? = ""

    @JsonField(name = ["cost"])
    var cost: Double? = 0.0

    @JsonField(name = ["type"])
    var type: Int? = 0

    @JsonField(name = ["sort"])
    var sort: Int? = 0

    @JsonField(name = ["status"])
    var status: Int? = 0

    @JsonField(name = ["customer_id"])
    var customer_id: Int? = 0

    @JsonField(name = ["advert_package_id"])
    var advert_package_id: Int? = 0

    @JsonField(name = ["media_length_by_second"])
    var media_length_by_second: Int? = 0

    @JsonField(name = ["play_count"])
    var play_count: Int? = 0

    @JsonField(name = ["is_running"])
    var is_running: Int? = 0

    @JsonField(name = ["media_url"])
    var media_url: Url? = null

    @JsonField(name = ["media_type"])
    var media_type: String? = ""

    @JsonField(name = ["from_hour"])
    var from_hour: Int? = 0

    @JsonField(name = ["to_hour"])
    var to_hour: Int? = 0

    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @JsonField(name = ["updated_at"])
    var updated_at: String? = ""
}