package vn.techres.line.data.model.mediaprofile

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class MediaProfileData : Serializable {

    @JsonField(name = ["limit"])
    var limit: Int? = 0

    @JsonField(name = ["list"])
    var list = ArrayList<MediaProfile>()

    @JsonField(name = ["total_record"])
    var total_record: Int? = 0
}