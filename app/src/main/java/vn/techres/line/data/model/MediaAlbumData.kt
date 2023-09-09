package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.mediaprofile.MediaProfile
import java.io.Serializable

class MediaAlbumData: Serializable {
    @JsonField(name = ["list"])
    var list =  ArrayList<MediaProfile>()
    @JsonField(name = ["limit"])
    var limit : Int? = 0
    @JsonField(name = ["total_record"])
    var total_record : Int? = 0

}