package vn.techres.line.holder.message.left

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemBusinessCardMessageLeftBinding
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.RevokeMessageHandler

class BusinessCardLeftHolder(private val binding: ItemBusinessCardMessageLeftBinding) :
    RecyclerView.ViewHolder(
        binding.root
    ) {
    fun bind(
        context: Context,
        configNodeJs: ConfigNodeJs,
        user: User,
        dataSources: ArrayList<MessagesByGroup>,
        position: Int,
        chatGroupHandler: ChatGroupHandler?,
        revokeMessageHandler: RevokeMessageHandler?
    ) {
        val message = dataSources[position]
        if(message.is_stroke == 1) {
            binding.rlBusinessCard.isSelected = true
        } else {
            binding.rlBusinessCard.isSelected = false
        }
        binding.tvNameUser.text = message.sender.full_name
        getImage(binding.imgAvatarUser, message.sender.avatar?.medium, configNodeJs)
//        binding.tvTimeBusinessCard.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        ChatUtils.checkTimeMessages(
            message.created_at,
            binding.tvTimeBusinessCard,
            position,
            dataSources
        )


        binding.tvNameContact.text = message.message_phone?.full_name
        val content = SpannableString(message.message_phone?.phone)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        binding.tvPhoneContact.text = content
        binding.tvContentPhoneContact.text = message.message_phone?.phone
        getImage(binding.imgAvatarContact, message.message_phone?.avatar?.medium, configNodeJs)
        if(message.message_phone?.member_id == CurrentUser.getCurrentUser(context).id) {
            binding.lnMain.visibility = View.GONE
            binding.viewMain.visibility = View.GONE
        } else {
            binding.lnMain.visibility = View.VISIBLE
            binding.viewMain.visibility = View.VISIBLE
            binding.tvCall.setOnClickListener {
                message.message_phone?.let { it1 -> chatGroupHandler?.onCallBusinessCard(it1) }
            }
            binding.tvChat.setOnClickListener {
                message.message_phone?.let { it1 -> chatGroupHandler?.onChatBusinessCard(it1) }
            }
        }
        binding.rlBusinessCard.setOnClickListener {
            message.message_phone?.member_id?.let { it1 ->
                chatGroupHandler?.onChooseAvatarBusinessCard(
                    it1
                )
            }
        }
        binding.tvPhoneContact.setOnClickListener {
            message.message_phone?.let { it1 -> chatGroupHandler?.onChooseBusinessCard(it1) }
        }
        binding.imgAvatarContact.setOnClickListener {
            message.message_phone?.member_id?.let { chatGroupHandler?.onChooseAvatarBusinessCard(it) }
        }

        if (!ChatUtils.checkSenderMessage(message, user)) {
            if (position >= 0 && (position + 1) < dataSources.size) {
                if (dataSources[position].sender.member_id == dataSources[position + 1].sender.member_id && message.today != 1&& dataSources[position + 1].message_type.equals(
                        TechResEnumChat.TYPE_BUSINESS_CARD.toString()
                    )) {
                    binding.tvNameUser.hide()
                    binding.cvUser.visibility = View.INVISIBLE
                } else {
                    binding.tvNameUser.show()
                    binding.cvUser.show()
                }
            }
        } else {
            binding.cvUser.hide()
            binding.tvNameUser.hide()
        }
        //reaction
        if (message.reactions.reactions_count == 0) {
            binding.reaction.lnReaction.hide()
            binding.reaction.imgReactionPress.show()
            binding.reaction.imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
            if (position > 0) {
                if (dataSources[position].sender.member_id == dataSources[position - 1].sender.member_id) {
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
        //timer
        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()
        ChatUtils.setViewTimeLeft(binding.reaction.lnReaction, binding.rlBusinessCard)

        itemView.setOnLongClickListener {
            revokeMessageHandler?.onRevoke(
                message,
                it
            )
            true
        }

        binding.reaction.lnReactionPress.setOnClickListener {
            chatGroupHandler?.onPressReaction(message, it)
        }
        binding.reaction.lnReaction.setOnClickListener {
            val reactionList = ChatUtils.getReactionItem(message.reactions)
            reactionList.sortByDescending { it.number }
            chatGroupHandler?.onClickViewReaction(message, reactionList)
        }
        binding.imgActionMore.setOnClickListener {
            chatGroupHandler?.onShareMessage(message)
        }

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