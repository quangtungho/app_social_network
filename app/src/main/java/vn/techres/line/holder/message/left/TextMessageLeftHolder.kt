package vn.techres.line.holder.message.left

import android.content.Context
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemTextChatLeftBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.ChatUtils.checkTimeMessages
import vn.techres.line.helper.utils.ChatUtils.isNumeric
import vn.techres.line.helper.utils.invisible
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.RevokeMessageHandler

class TextMessageLeftHolder(val binding: ItemTextChatLeftBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        configNodeJs: ConfigNodeJs,
        user: User,
        position: Int,
        dataSource: ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?,
        revokeMessageHandler: RevokeMessageHandler?,
        chooseNameUserHandler: ChooseNameUserHandler?
    ) {
        val message = dataSource[position]
        binding.rlMessage.isSelected = message.is_stroke == 1
        checkTimeMessages(message.created_at, binding.tvTimeText, position, dataSource)
//        binding.tvTimeText.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        binding.tvNameText.text = message.sender.full_name
        getImage(binding.imgAvatar, message.sender.avatar?.medium, configNodeJs)

        ChatUtils.setClickTagName(
            message.message ?: "",
            message.list_tag_name,
            binding.tvMessage,
            chooseNameUserHandler
        )

        if (message.message?.let { isNumeric(it) } == true && message.message?.length!! > 6 && message.message?.length!! < 12) {
            binding.tvMessage.setPaintFlags(binding.tvMessage.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
            binding.tvMessage.setTextColor(context.resources.getColor(R.color.blue))
            binding.tvMessage.setOnClickListener { chatGroupHandler?.onClickTextPhone(message) }
        }
        if (!ChatUtils.checkSenderMessage(message, user)) {
            if (position >= 0 && (position + 1) < dataSource.size) {
                if (dataSource[position].sender.member_id == dataSource[position + 1].sender.member_id
                    && message.today != 1
                    && (dataSource[position + 1].message_type.equals(
                        TechResEnumChat.TYPE_TEXT.toString()
                    ) || dataSource[position + 1].message_type.equals(
                        TechResEnumChat.TYPE_REPLY.toString()
                    ))
                ) {
                    binding.tvNameText.hide()
                    binding.cvText.invisible()
                } else {
                    binding.tvNameText.show()
                    binding.cvText.show()
                }
            }
        } else {
            binding.cvText.hide()
            binding.tvNameText.hide()
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

        ChatUtils.setViewTimeLeft(binding.reaction.lnReaction, binding.rlMessage)

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
        if (bindingAdapterPosition == 0) {
            binding.rcViewer.hide()
            binding.tvStatusView.show()
            binding.tvStatusView.text = context.getString(R.string.received)
            message.message_viewed.let {
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