package vn.techres.line.data.model.notification

import vn.techres.line.data.model.chat.Reaction
import vn.techres.line.data.model.chat.Reply

class MessageUpdateNotification(
    var reactions: Reaction,
    var message_reply: Reply,
    var is_revoke: Int,
    var type_message : Int,
    var random_key: String,
    var group_id: String
)