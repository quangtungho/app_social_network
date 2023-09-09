package vn.techres.line.interfaces

import vn.techres.line.data.model.chat.Group


interface GroupChatHandler {
    fun onChat(group: Group)
    fun onBottomSheet(group: Group, position : Int)
    fun onCall(group: Group)
}