package vn.techres.line.holder.message.right

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.databinding.ItemLinkChatRightBinding
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.getMediaGlide
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.RevokeMessageHandler

class LinkMessageRightHolder(private val binding: ItemLinkChatRightBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        dataSource: ArrayList<MessagesByGroup>,
        position: Int,
        chatGroupHandler: ChatGroupHandler?,
        revokeMessageHandler: RevokeMessageHandler?,
        chooseNameUserHandler: ChooseNameUserHandler?
    ) {
        val message = dataSource[position]
        if(message.is_stroke == 1) {
            binding.lnLink.isSelected = true
            binding.lnLink.setPadding(2,6,2,6)
        } else {
            binding.lnLink.isSelected = false
            binding.lnLink.setPadding(0,6,0,6)
        }
//        binding.tvTimeLink.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeLink, position, dataSource)

        if (message.message_share != null && !StringUtils.isNullOrEmpty(message.message_share?.random_key)) {
            setLink(
                context,
                message.message_share?.message_link?.author ?: "",
                message.message_share?.message_link?.title ?: "",
                message.message_share?.message_link?.description ?: "",
                message.message_share?.message_link?.cannonical_url ?: ""
            )
            if (StringUtils.isNullOrEmpty(message.message_share?.message)) {
                binding.tvMessage.hide()
            } else {
                binding.tvMessage.show()
                binding.tvMessage.text = message.message_share?.message
            }
            getMediaGlide(
                binding.link.imgLinkPreview,
                message.message_share?.message_link?.media_thumb
            )
        } else {
            setLink(
                context, message.message_link?.author ?: "", message.message_link?.title ?: "",
                message.message_link?.description ?: "", message.message_link?.url ?: ""
            )
            if (StringUtils.isNullOrEmpty(message.message)) {
                binding.tvMessage.hide()
            } else {
                binding.tvMessage.show()
                binding.tvMessage.text = message.message
            }
            getMediaGlide(binding.link.imgLinkPreview, message.message_link?.media_thumb)
        }
        if (message.reactions.reactions_count == 0) {
            binding.reaction.lnReaction.hide()
            binding.reaction.imgReactionPress.show()
            binding.reaction.imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
            if (position > 0) {
                if (dataSource[position].sender.member_id == dataSource[position - 1].sender.member_id) {
                    binding.lnReactionContainer.hide()
                } else {
                    binding.lnReactionContainer.show()
                }
            } else {
                binding.lnReactionContainer.show()
            }
        } else {
            ChatUtils.setReaction(
                binding.lnReactionContainer,
                binding.reaction,
                message.reactions
            )
        }

        binding.reaction.lnReactionPress.setOnClickListener {
            chatGroupHandler?.onPressReaction(message, it)
        }
        binding.reaction.lnReaction.setOnClickListener {
            val reactionList = ChatUtils.getReactionItem(message.reactions)
            reactionList.sortByDescending { it.number }
            chatGroupHandler?.onClickViewReaction(message, reactionList)
        }

        itemView.setOnLongClickListener {
            revokeMessageHandler?.onRevoke(message, it)
            true
        }
        //timer
        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()
        ChatUtils.setViewTimeRight(binding.reaction.lnReaction, binding.lnLink)

        /**
         * Message viewed
         * */
        if (bindingAdapterPosition == 0) {
            binding.rcViewer.hide()
            binding.tvStatusView.show()
            binding.tvStatusView.text = context.getString(R.string.received)
            message.message_viewed?.let {
                ChatUtils.setMessageViewer(context, binding.rcViewer, it)
                binding.rcViewer.show()
                binding.tvStatusView.hide()
            }
        } else {
            binding.rcViewer.hide()
            binding.tvStatusView.hide()
        }
        /**
         * Tag name
         * */
        ChatUtils.setClickTagName(
            message.message.toString(),
            message.list_tag_name,
            binding.tvMessage,
            chooseNameUserHandler
        )
    }

    private fun setLink(
        context: Context,
        author: String,
        title: String,
        description: String,
        cannonicalUrl: String
    ) {
        if (StringUtils.isNullOrEmpty(author)) {
            binding.link.tvAuthorLink.hide()
        } else {
            binding.link.tvAuthorLink.show()
            binding.link.tvAuthorLink.text = author
        }
        if (StringUtils.isNullOrEmpty(title)) {
            binding.link.tvTitleLink.hide()
        } else {
            binding.link.tvTitleLink.show()
            binding.link.tvTitleLink.text = title
        }

        if (StringUtils.isNullOrEmpty(description)) {
            binding.link.tvDescriptionLink.hide()
        } else {
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