package vn.techres.line.call

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/5/2022
 */

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class SocketChangeCameraOrMicRequest : Serializable {
    @JsonField(name = ["member_id"])
    var member_id : Int? = 0

    @JsonField(name = ["status"])
    var status : Int? = 0

    @JsonField(name = ["group_id"])
    var group_id : String? = ""
}