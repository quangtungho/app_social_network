package vn.techres.line.holder.message

import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemChatGiftNotificationBinding
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.interfaces.gift.ChatGiftHandler

/**
 * @Author: Phạm Văn Nhân
 * @Date: 26/04/2022
 */
class ChatGiftNotificationHolder(private val binding: ItemChatGiftNotificationBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        configNodeJs: ConfigNodeJs,
        messagesByGroup: MessagesByGroup,
        chatGiftHandler: ChatGiftHandler,
        typeMessage: String
    ) {
        if (typeMessage != TechResEnumChat.TYPE_MESSAGE_INFORMATION.toString()){
            itemView.setOnClickListener {
                messagesByGroup.message_information?.let { it1 -> chatGiftHandler.onGetGift(it1) }
            }
        }

        ChatUtils.getGlide(binding.imgGiftBanner, messagesByGroup.message_information!!.banner, configNodeJs)
        ChatUtils.getGlide(binding.imgGiftAvatar, messagesByGroup.message_information!!.avatar, configNodeJs)
        binding.txtGiftTitle.text = messagesByGroup.message_information!!.title
        binding.txtGiftContent.text = messagesByGroup.message_information!!.content
        binding.txtExpiry.text = String.format("HSD: %s", messagesByGroup.message_information!!.expire_at)
    }
}