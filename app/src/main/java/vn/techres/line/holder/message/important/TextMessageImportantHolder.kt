package vn.techres.line.holder.message.important

import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemTextMessageImportantBinding
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.utils.loadImage
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.MessageImportantHandler

class TextMessageImportantHolder(private val binding : ItemTextMessageImportantBinding) : RecyclerView.ViewHolder(binding.root){
    fun bind(configNodeJs: ConfigNodeJs, message : MessagesByGroup, messageImportantHandler: MessageImportantHandler?, chooseNameUserHandler: ChooseNameUserHandler?){
        binding.imgAvatar.loadImage(String.format("%s%s", configNodeJs.api_ads, message.sender.avatar?.medium), R.drawable.ic_user_placeholder_circle, true)
        binding.tvName.text = message.sender.full_name
        binding.tvDateMessage.text = TimeFormatHelper.getDateFromStringDayMonthYear(
            message.created_at
        )
        if(message.message_share != null && !StringUtils.isNullOrEmpty(message.message_share?._id)){
            ChatUtils.setClickTagName(
                message.message_share?.message ?: "",
                message.message_share?.list_tag_name ?: ArrayList(),
                binding.tvMessage,
                chooseNameUserHandler
            )
        }else{
            ChatUtils.setClickTagName(
                message.message.toString(),
                message.list_tag_name,
                binding.tvMessage,
                chooseNameUserHandler
            )
        }

        binding.tvMessage.setOnLongClickListener {

            true
        }
        binding.imgAvatar.setOnClickListener {
            messageImportantHandler?.onChooseSender(message.sender)
        }
        binding.tvName.setOnClickListener {
            messageImportantHandler?.onChooseSender(message.sender)
        }
        binding.imgStart.setOnClickListener {
            messageImportantHandler?.onRemoveMessage(message)
        }
    }
}