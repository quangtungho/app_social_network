package vn.techres.line.interfaces.chat

import vn.techres.line.data.model.chat.MessageViewer

interface ViewerMessageHandler {
    fun onChooseViewer(messageViewer: MessageViewer)
}