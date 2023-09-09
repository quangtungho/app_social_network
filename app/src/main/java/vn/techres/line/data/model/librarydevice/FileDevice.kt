package vn.techres.line.data.model.librarydevice

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class FileDevice : Serializable {
    @JsonField(name = ["path"])
    var path: String? = ""

    @JsonField(name = ["file_name"])
    var file_name: String? = ""

    @JsonField(name = ["status_new"])
    var status_new: Int? = 0

    @JsonField(name = ["format"])
    var format: String? = ""

    @JsonField(name = ["type"])
    var type: String? = ""
}