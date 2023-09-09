package vn.techres.line.holder.message.right

import android.content.Context
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.databinding.ItemTextChatRightBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.ChatUtils.isNumeric
import vn.techres.line.helper.utils.ChatUtils.setViewTimeRight
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.RevokeMessageHandler

class TextMessageRightHolder(val binding: ItemTextChatRightBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        position: Int,
        dataSource: ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?,
        revokeMessageHandler: RevokeMessageHandler?,
        chooseNameUserHandler: ChooseNameUserHandler?
    ) {
        val message = dataSource[position]
        if(message.is_stroke == 1) {
            binding.rlMessage.isSelected = true
        } else {
            binding.rlMessage.isSelected = false
        }
        //set message
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeText, position, dataSource)

        ChatUtils.setClickTagName(
            message.message ?: "",
            message.list_tag_name,
            binding.tvMessage,
            chooseNameUserHandler
        )

        if(message.message?.let { isNumeric(it) } == true && message.message?.length!! > 6 && message.message?.length!! < 12) {
            binding.tvMessage.paintFlags = binding.tvMessage.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            binding.tvMessage.setTextColor(context.resources.getColor(R.color.blue))
            binding.tvMessage.setOnClickListener { chatGroupHandler?.onClickTextPhone(message) }
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
        //
//        val lp = binding.rlMessage.layoutParams as ViewGroup.MarginLayoutParams
//        if (binding.reaction.lnReaction.visibility == View.VISIBLE) {
//            lp.setMargins(dpToPx(8f), 0, 0, dpToPx(14f))
//        } else {
//
//            lp.setMargins(dpToPx(8f), 0, 0, dpToPx(4f))
//        }
        setViewTimeRight(binding.reaction.lnReaction, binding.rlMessage)

        binding.reaction.lnReactionPress.setOnClickListener {
            chatGroupHandler?.onPressReaction(message, it)
        }
        binding.reaction.lnReaction.setOnClickListener {
            val reactionList = ChatUtils.getReactionItem(message.reactions)
            reactionList.sortByDescending { it.number }
            chatGroupHandler?.onClickViewReaction(message, reactionList)
        }

        itemView.setOnLongClickListener {
            revokeMessageHandler?.onRevoke(
                message,
                it
            )
            true
        }

        //timer
        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()
        /**
         * Message viewed
         * */
        if(bindingAdapterPosition == 0){
            binding.rcViewer.hide()
            binding.tvStatusView.show()
            binding.tvStatusView.text = context.getString(R.string.received)
            message.message_viewed.let {
                ChatUtils.setMessageViewer(context, binding.rcViewer, it)
                binding.rcViewer.show()
                binding.tvStatusView.hide()
            }
        }else{
            binding.rcViewer.hide()
            binding.tvStatusView.hide()
        }

        if (message.offline == 0){
            binding.tvActiveStatus.hide()
        }else{
            binding.tvActiveStatus.show()
            binding.lnReactionContainer.hide()
        }

    }
}