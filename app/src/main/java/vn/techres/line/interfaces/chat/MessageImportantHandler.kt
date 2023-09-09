package vn.techres.line.interfaces.chat

import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.chat.PhoneMessage
import vn.techres.line.data.model.chat.Sender

interface MessageImportantHandler {
    fun onChooseBusinessCard(phoneMessage : PhoneMessage)
    fun onChooseLink(messagesByGroup: MessagesByGroup)
    fun onRemoveMessage(messagesByGroup: MessagesByGroup)
    fun onChooseAvatarBusinessCard(userId : Int)
    fun onOpenFile(path : String?)
    fun onDeleteDownLoadFile(nameFile : String?)
    fun onCopyMessage(messagesByGroup: MessagesByGroup)
    fun onChooseSender(sender: Sender)
}