package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class MessageCustomer : Serializable {

    @JsonField(name = ["node_refesh_token"])
    var node_refesh_token: String? = ""

    @JsonField(name = ["node_access_token"])
    var node_access_token: String? = ""

    @JsonField(name = ["_id"])
    var _id: String? = ""

    @JsonField(name = ["search_key"])
    var search_key: String? = ""

    @JsonField(name = ["is_online"])
    var is_online: Int? = 0

}