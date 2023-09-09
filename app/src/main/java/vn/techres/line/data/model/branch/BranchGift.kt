package vn.techres.line.data.model.branch

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BranchGift : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["phone_number"])
    var phone_number: String? = ""

    @JsonField(name = ["image_logo_url"])
    var image_logo_url: String? = ""

    @JsonField(name = ["banner_image_url"])
    var banner_image_url: String? = ""

    @JsonField(name = ["address_full_text"])
    var address_full_text: String? = ""

    @JsonField(name = ["lat"])
    var lat: String? = ""

    @JsonField(name = ["lng"])
    var lng: String? = ""

    @JsonField(name = ["image_urls"])
    var image_urls = ""


}