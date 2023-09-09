package vn.techres.line.holder.message

import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemGettingChatBinding
import vn.techres.line.helper.utils.ChatUtils

/**
 * @Author: Phạm Văn Nhân
 * @Date: 29/04/2022
 */
class GettingChatHolder(private val binding: ItemGettingChatBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        configNodeJs: ConfigNodeJs,
        messagesByGroup: MessagesByGroup,
    ) {
        ChatUtils.getGlide(binding.imgPhoto, messagesByGroup.message_gettings!!.image_getting, configNodeJs)
        binding.txtContent.text = messagesByGroup.message_gettings!!.message
    }
}