package vn.techres.line.interfaces.chat

import vn.techres.line.data.model.chat.MessagesByGroup

interface PinnedDetailHandler {
    fun onRemove(messagesByGroup: MessagesByGroup)
    fun onPinRetry(messagesByGroup: MessagesByGroup)
    fun onPinUnpin(messagesByGroup: MessagesByGroup)
}