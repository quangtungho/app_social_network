package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 22/04/2022
 */
@JsonObject
class CallDataRequest : Serializable {
    @JsonField(name = ["groupID"])
    var groupID: String? = ""

    @JsonField(name = ["senderID"])
    var senderID: String? = ""

    @JsonField(name = ["memberID"])
    var memberID: Int? = 0

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["name"])
    var name: String? = ""

    @JsonField(name = ["notificationID"])
    var notificationID: Int? = 0

    @JsonField(name = ["messageType"])
    var messageType: String? = ""

    @JsonField(name = ["keyRoom"])
    var keyRoom: String? = ""
}