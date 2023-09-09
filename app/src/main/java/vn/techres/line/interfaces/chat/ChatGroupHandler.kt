package vn.techres.line.interfaces.chat

import android.view.View
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.chat.PhoneMessage
import vn.techres.line.data.model.chat.ReactionItem

interface ChatGroupHandler {
    fun onPressReaction(messagesByGroup: MessagesByGroup, view : View)
    fun onClickViewReaction(messagesByGroup: MessagesByGroup, reactionItems: ArrayList<ReactionItem>)
    fun onOpenFile(path : String?)
    fun onDeleteDownLoadFile(nameFile : String?)
    fun onAddMember()
    fun onChooseAvatarBusinessCard(userId : Int)
    fun onChatBusinessCard(phoneMessage : PhoneMessage)
    fun onCallBusinessCard(phoneMessage : PhoneMessage)
    fun onChooseBusinessCard(phoneMessage : PhoneMessage)
    fun onChooseCallVideo(messagesByGroup: MessagesByGroup)
    fun onScrollMessage(messagesByGroup: MessagesByGroup)
    fun onShareMessage(messagesByGroup: MessagesByGroup)
    fun onIntentSendBirthDayCard(messagesByGroup: MessagesByGroup)
    fun onReviewInviteCard(messagesByGroup: MessagesByGroup)
    fun onClickTextPhone(messagesByGroup: MessagesByGroup)
    fun onClickHideKeyboard()
}