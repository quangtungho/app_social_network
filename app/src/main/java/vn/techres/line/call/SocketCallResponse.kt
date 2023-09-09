package vn.techres.line.call

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/3/2022
 */

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class SocketCallResponse : Serializable {

    @JsonField(name = ["member_id"])
    var member_id : Int? = 0

    @JsonField(name = ["message_type"])
    var message_type : String? = ""

    @JsonField(name = ["call_member_create"])
    var call_member_create : Int? = 0

    @JsonField(name = ["member_receiver_id"])
    var member_receiver_id : Int? = 0

    @JsonField(name = ["group_id"])
    var group_id : String? = ""

    @JsonField(name = ["key_room"])
    var key_room : String? = ""

    @JsonField(name = ["avatar"])
    var avatar : String? = null

    @JsonField(name = ["full_name"])
    var full_name : String? = ""

    @JsonField(name = ["app_name"])
    var app_name : String? = "aloline"

    @JsonField(name = ["group_id_for_notification"])
    var group_id_for_notification : Int? = 0

    constructor(
        member_id: Int?,
        full_name: String?,
        avatar: String?,
        group_id: String?,
        message_type: String?,
        key_room: String?,
        call_member_create: Int?,
        group_id_for_notification: Int?,
    ) {
        this.member_id = member_id
        this.message_type = message_type
        this.call_member_create = call_member_create
        this.group_id = group_id
        this.key_room = key_room
        this.avatar = avatar
        this.full_name = full_name
        this.group_id_for_notification = group_id_for_notification
    }
}