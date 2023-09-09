package vn.techres.line.data.model.album

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class ImageFolder: Serializable {
    constructor(_id: String, link: String, status: Boolean) {
        this._id = _id
        this.link = link
        this.status = status
    }
    constructor()
    @JsonField(name = ["_id"])
    var _id : String = ""
    @JsonField(name = ["link"])
    var link : String = ""
    @JsonField(name = ["status"])
    var status : Boolean = false
}