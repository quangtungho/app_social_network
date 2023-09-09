package vn.techres.line.holder.message.important

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemAudioMessageImportantBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.utils.loadImage
import vn.techres.line.interfaces.chat.MessageImportantHandler

class AudioMessageImportantHolder(private val binding : ItemAudioMessageImportantBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(context : Context, configNodeJs : ConfigNodeJs, message : MessagesByGroup, messageImportantHandler : MessageImportantHandler?){
        binding.imgAvatar.loadImage(String.format("%s%s", configNodeJs.api_ads, message.sender.avatar?.medium), R.drawable.ic_user_placeholder_circle, true)
        binding.tvName.text = message.sender.full_name
        binding.tvDateMessage.text = TimeFormatHelper.getDateFromStringDayMonthYear(
            message.created_at
        )

        binding.imgStart.setOnClickListener {
            messageImportantHandler?.onRemoveMessage(message)
        }
        binding.imgAvatar.setOnClickListener {
            messageImportantHandler?.onChooseSender(message.sender)
        }
        binding.tvName.setOnClickListener {
            messageImportantHandler?.onChooseSender(message.sender)
        }
    }
}