package vn.techres.line.interfaces.chat

import android.view.View
import vn.techres.line.data.model.chat.MessagesByGroup

interface RevokeMessageHandler {
    fun onRevoke(messagesByGroup: MessagesByGroup, view : View)
}