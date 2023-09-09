package vn.techres.line.holder.message.right

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemInviteEventCardRightChatBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.RevokeMessageHandler

class InviteEventMessageRightHolder(
    val binding: ItemInviteEventCardRightChatBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        configNodeJs : ConfigNodeJs,
        position: Int,
        dataSource: ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?,
        revokeMessageHandler: RevokeMessageHandler?
    ) {
        val message = dataSource[position]
        if(message.is_stroke == 1) {
            binding.rltInviteCard.isSelected = true
        } else {
            binding.rltInviteCard.isSelected = false
        }
//        binding.tvTimeAudio.text = TimeFormatHelper.getDateFromStringDay(
//            message.created_at
//        )
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeAudio, position, dataSource)

        /**
         * time message
         * */
        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()
        /**
         * data
         * */
        binding.inviteCard.txtMessage.text = dataSource[position].message_event?.info_receiver?.full_name
        Utils.getGlide(binding.inviteCard.imvAvatar, dataSource[position].message_event?.info_receiver?.avatar?.original, configNodeJs)
        Utils.getGlide(binding.inviteCard.background, dataSource[position].files[0].link_original, configNodeJs)
        /**
         * Set reaction
         * */
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

//        ChatUtils.setViewTimeRight(binding.reaction.lnReaction, binding.rltInviteCard)

        binding.reaction.lnReactionPress.setOnClickListener {
            chatGroupHandler?.onPressReaction(message, it)
        }
        binding.reaction.lnReaction.setOnClickListener {
            val reactionList = ChatUtils.getReactionItem(message.reactions)
            reactionList.sortByDescending { it.number }
            chatGroupHandler?.onClickViewReaction(message, reactionList)
        }

        /**
         * revoke message
         * */
        itemView.setOnLongClickListener {
            revokeMessageHandler?.onRevoke(
                message,
                it
            )
            true
        }
        binding.inviteCard.txtOpenCard.setOnClickListener {
            chatGroupHandler?.onReviewInviteCard(message)
        }
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

    }
}