package vn.techres.line.data.model.chat.videocall.response

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class IceCandidateResponse : Serializable {
//    @JsonField(name = ["ice_candidate"])
//    var ice_candidate : IceCandidate? = null

    @JsonField(name = ["member_id"])
    var member_id : Int? = 0

    @JsonField(name = ["label"])
    var label : Int? = 0

    @JsonField(name = ["id"])
    var id : String? = ""

    @JsonField(name = ["candidate"])
    var candidate : String? = ""
}