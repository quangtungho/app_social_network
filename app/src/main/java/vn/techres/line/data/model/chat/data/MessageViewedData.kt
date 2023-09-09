package vn.techres.line.data.model.chat.data

import vn.techres.line.data.model.chat.MessageViewer

class MessageViewedData {
    var list_user_seen = ArrayList<MessageViewer>()
    var list_user_not_seen = ArrayList<MessageViewer>()
}