package vn.techres.line.holder.message.important

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemLinkMessageImportantBinding
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.loadImage
import vn.techres.line.interfaces.chat.MessageImportantHandler

class LinkMessageImportantHolder(private val binding : ItemLinkMessageImportantBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(context : Context, configNodeJs: ConfigNodeJs, message : MessagesByGroup, messageImportantHandler : MessageImportantHandler?){
        binding.imgAvatar.loadImage(String.format("%s%s", configNodeJs.api_ads, message.sender.avatar?.medium), R.drawable.ic_user_placeholder_circle, true)
        binding.tvName.text = message.sender.full_name
        binding.tvDateMessage.text = TimeFormatHelper.getDateFromStringDayMonthYear(
            message.created_at
        )
        if(message.message_share != null && !StringUtils.isNullOrEmpty(message.message_share?.random_key)){
            setLink(context, message.message_share?.message ?: "", message.message_share?.message_link?.author ?: "", message.message_share?.message_link?.title ?: "",
                message.message_share?.message_link?.description ?: "", message.message_share?.message_link?.cannonical_url ?: ""
            )
        }else{
            setLink(context, message.message ?: "", message.message_link?.author ?: "", message.message_link?.title ?: "",
                message.message_link?.description ?: "", message.message_link?.cannonical_url ?: ""
            )
        }
        binding.lnLink.setOnClickListener {
            messageImportantHandler?.onChooseLink(message)
        }
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

    private fun setLink(context: Context, message : String, author : String, title : String, description : String, cannonicalUrl : String){
        if(StringUtils.isNullOrEmpty(message)){
            binding.tvMessage.hide()
        }else{
            binding.tvMessage.show()
            binding.tvMessage.text = message
        }
        if(StringUtils.isNullOrEmpty(author)){
            binding.link.tvAuthorLink.hide()
        }else{
            binding.link.tvAuthorLink.show()
            binding.link.tvAuthorLink.text = author
        }
        if(StringUtils.isNullOrEmpty(title)){
            binding.link.tvTitleLink.hide()
        }else{
            binding.link.tvTitleLink.show()
            binding.link.tvTitleLink.text = title
        }

        if(StringUtils.isNullOrEmpty(description)){
            binding.link.tvDescriptionLink.hide()
        }else{
            binding.link.tvDescriptionLink.show()
            binding.link.tvDescriptionLink.text = description
        }
        binding.link.rlLink.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(cannonicalUrl))
            context.startActivity(browserIntent)
        }

    }
}