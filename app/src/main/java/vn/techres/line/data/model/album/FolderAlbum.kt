package vn.techres.line.data.model.album

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class FolderAlbum() : Serializable {
    @JsonField(name = ["link"])
    var link: String = ""
    @JsonField(name = ["folder_name"])
    var folder_name: String = ""
    @JsonField(name = ["_id"])
    var _id: String = ""
    @JsonField(name = ["user_id"])
    var user_id: Int = 0
    @JsonField(name = ["is_share"])
    var is_share: Int = 0
}