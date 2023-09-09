package vn.techres.line.interfaces.chat

import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.chat.response.TypingUser
import vn.techres.line.data.model.chat.videocall.response.ConnectCallPersonalResponse

interface SocketPersonalHandler {
    fun onChatText(messagesByGroup: MessagesByGroup)
    fun onChatImage(messagesByGroup: MessagesByGroup)
    fun onChatVideo(messagesByGroup: MessagesByGroup)
    fun onChatAudio(messagesByGroup: MessagesByGroup)
    fun onChatFile(messagesByGroup: MessagesByGroup)
    fun onSticker(messagesByGroup: MessagesByGroup)
    fun onRevokeMessage(messagesByGroup: MessagesByGroup)
    fun onReactionMessage(messagesByGroup: MessagesByGroup)
    fun onPinned(messagesByGroup: MessagesByGroup)
    fun onPinnedMessageText(messagesByGroup: MessagesByGroup)
    fun onRevokePinnedMessageText(messagesByGroup: MessagesByGroup)
    fun onRevokePinned()
    fun onReply(messagesByGroup: MessagesByGroup)
    fun onRevokeReply(messagesByGroup: MessagesByGroup)
    fun onTypingOn(typingUser: TypingUser)
    fun onTypingOff(typingUser: TypingUser)
    fun onUpdateBackground(messagesByGroup: MessagesByGroup)
    fun onConnectCall(connectCallPersonalResponse: ConnectCallPersonalResponse)
    fun onVideoCallMessage(messagesByGroup: MessagesByGroup)
    fun onAudioCallMessage(messagesByGroup: MessagesByGroup)
    fun onShare(messagesByGroup: MessagesByGroup)
    fun onShareNote(messagesByGroup: MessagesByGroup)
    fun onBusinessCard(messagesByGroup: MessagesByGroup)
}