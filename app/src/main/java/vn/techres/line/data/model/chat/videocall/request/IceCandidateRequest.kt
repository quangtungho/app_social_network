package vn.techres.line.data.model.chat.videocall.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class IceCandidateRequest : Serializable {
//    @JsonField(name = ["ice_candidate"])
//    var ice_candidate : IceCandidate? = null

    @JsonField(name = ["member_id"])
    var member_id : Int? = 0

    @JsonField(name = ["group_id"])
    var group_id : String? = ""

    @JsonField(name = ["label"])
    var label : Int? = 0

    @JsonField(name = ["id"])
    var id : String? = ""

    @JsonField(name = ["candidate"])
    var candidate : String? = ""
}