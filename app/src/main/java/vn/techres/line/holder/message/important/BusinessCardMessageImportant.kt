package vn.techres.line.holder.message.important

import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemBusinessCardMessageImportantBinding
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.utils.loadImage
import vn.techres.line.interfaces.chat.MessageImportantHandler

class BusinessCardMessageImportant(private val binding : ItemBusinessCardMessageImportantBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(configNodeJs: ConfigNodeJs, message : MessagesByGroup, messageImportantHandler : MessageImportantHandler?){
        binding.imgAvatar.loadImage(String.format("%s%s", configNodeJs.api_ads, message.sender.avatar?.medium), R.drawable.ic_user_placeholder_circle, true)
        binding.tvName.text = message.sender.full_name
        binding.tvDateMessage.text = TimeFormatHelper.getDateFromStringDayMonthYear(
            message.created_at
        )

        if (message.message_share != null && !StringUtils.isNullOrEmpty(message.message_share?.random_key)){
            binding.tvNameContact.text = message.message_share?.message_phone?.full_name
            binding.tvContentPhoneContact.text = message.message_share?.message_phone?.phone
            binding.imgAvatarContact.loadImage(String.format("%s%S", configNodeJs.api_ads, message.message_share?.message_phone?.avatar?.medium), R.drawable.ic_user_placeholder_circle, true)
            binding.rlBusinessCard.setOnClickListener {
                message.message_share?.message_phone?.let { it1 -> messageImportantHandler?.onChooseBusinessCard(it1) }
            }
            binding.tvContentPhoneContact.setOnClickListener {
                message.message_share?.message_phone?.let { it1 -> messageImportantHandler?.onChooseBusinessCard(it1) }
            }
            binding.imgAvatarContact.setOnClickListener {
                message.message_share?.message_phone?.member_id?.let {
                    messageImportantHandler?.onChooseAvatarBusinessCard(it)
                }
            }
        }else{
            binding.tvNameContact.text = message.message_phone?.full_name
            binding.tvContentPhoneContact.text = message.message_phone?.phone
            Utils.getImage(
                binding.imgAvatarContact,
                message.message_phone?.avatar?.medium,
                configNodeJs
            )
            binding.imgAvatarContact.loadImage(String.format("%s%S", configNodeJs.api_ads, message.message_phone?.avatar?.medium), R.drawable.ic_user_placeholder_circle, true)
            binding.rlBusinessCard.setOnClickListener {
                message.message_phone?.let { it1 -> messageImportantHandler?.onChooseBusinessCard(it1) }
            }
            binding.tvContentPhoneContact.setOnClickListener {
                message.message_phone?.let { it1 -> messageImportantHandler?.onChooseBusinessCard(it1) }
            }
            binding.imgAvatarContact.setOnClickListener {
                message.message_phone?.member_id?.let {
                    messageImportantHandler?.onChooseAvatarBusinessCard(it)
                }
            }
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