package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.utils.Url
import java.io.Serializable

class MediaProfile : Serializable {
    @JsonField(name = ["id"])
    var id : Int? = 0

    @JsonField(name = ["name"])
    var name : String? = ""

    @JsonField(name = ["url"])
    var url = Url()

    @JsonField(name = ["avatar"])
    var avatar : String? = ""

    @JsonField(name = ["sort"])
    var sort : Int? = 0

    @JsonField(name = ["status"])
    var status : Int? = 0

    @JsonField(name = ["total"])
    var total : Int? = 0

    @JsonField(name = ["customer_id"])
    var customer_id : Int? = 0

    @JsonField(name = ["customer_media_album_id"])
    var customer_media_album_id : Int? = 0

    @JsonField(name = ["media_type"])
    var media_type : Int? = 0

    @JsonField(name = ["created_at"])
    var created_at : String? = ""
}