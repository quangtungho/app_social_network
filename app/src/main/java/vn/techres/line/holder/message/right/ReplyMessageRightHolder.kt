package vn.techres.line.holder.message.right

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemReplyChatRightBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.getGlide
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.Utils.showImage
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.FileUtils.getMimeType
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.RevokeMessageHandler

class ReplyMessageRightHolder(val binding: ItemReplyChatRightBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        configNodeJs: ConfigNodeJs,
        position: Int,
        dataSource: ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?,
        revokeMessageHandler: RevokeMessageHandler?,
        chooseNameUserHandler: ChooseNameUserHandler?
    ) {
        val message = dataSource[position]
        if (message.is_stroke == 1) {
            binding.lnReply.isSelected = true
        } else {
            binding.lnReply.isSelected = false
        }
        //set message Reply
        ChatUtils.setClickTagName(
            message.message.toString(),
            message.list_tag_name,
            binding.tvMessageReply,
            chooseNameUserHandler
        )
        //
//        binding.tvTimeReply.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeReply, position, dataSource)

        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()

        binding.reply.tvReplyName.text = message.message_reply?.sender?.full_name ?: ""

        if (message.message_reply?.status == 1) {
            when (message.message_reply?.message_type) {
                TechResEnumChat.TYPE_TEXT.toString(),
                TechResEnumChat.TYPE_REPLY.toString() -> {
                    binding.reply.rlReplyThumbContainer.hide()
                    ChatUtils.setClickTagName(
                        (message.message_reply?.message ?: ""),
                        message.message_reply?.list_tag_name ?: ArrayList(),
                        binding.reply.tvReplyContent,
                        chooseNameUserHandler
                    )
                }
                TechResEnumChat.TYPE_IMAGE.toString() -> {
                    binding.reply.rlReplyThumbContainer.show()
                    binding.reply.cvMediaReply.show()
                    binding.reply.imgReplyPlay.hide()
                    binding.reply.imgReplyAudio.hide()
                    binding.reply.imgFile.hide()
                    binding.reply.tvReplyContent.text =
                        context.resources.getString(R.string.pinned_image)

                    getGlide(
                        binding.reply.imgReply,
                        message.message_reply!!.files[0].link_original,
                        configNodeJs
                    )
                }
                TechResEnumChat.TYPE_VIDEO.toString() -> {
                    binding.reply.rlReplyThumbContainer.show()
                    binding.reply.cvMediaReply.show()
                    binding.reply.imgReplyPlay.show()
                    binding.reply.imgReplyAudio.hide()
                    binding.reply.imgFile.hide()
                    binding.reply.tvReplyContent.text =
                        context.resources.getString(R.string.pinned_video)

                    getGlide(
                        binding.reply.imgReply,
                        message.message_reply!!.files[0].link_original,
                        configNodeJs
                    )
                }
                TechResEnumChat.TYPE_STICKER.toString() -> {
                    binding.reply.rlReplyThumbContainer.show()
                    binding.reply.cvMediaReply.show()
                    binding.reply.imgReplyPlay.hide()
                    binding.reply.imgReplyAudio.hide()
                    binding.reply.imgFile.hide()
                    binding.reply.tvReplyContent.text =
                        context.resources.getString(R.string.pinned_sticker)

                    getGlide(
                        binding.reply.imgReply,
                        message.message_reply!!.files[0].link_original,
                        configNodeJs
                    )
                }
                TechResEnumChat.TYPE_AUDIO.toString() -> {
                    binding.reply.rlReplyThumbContainer.show()
                    binding.reply.cvMediaReply.hide()
                    binding.reply.imgReplyPlay.hide()
                    binding.reply.imgReplyAudio.show()
                    binding.reply.imgFile.hide()
                    binding.reply.tvReplyContent.text =
                        context.resources.getString(R.string.pinned_audio)
                }
                TechResEnumChat.TYPE_FILE.toString() -> {
                    binding.reply.rlReplyThumbContainer.show()
                    binding.reply.cvMediaReply.hide()
                    binding.reply.imgReplyPlay.hide()
                    binding.reply.imgReplyAudio.hide()
                    binding.reply.imgFile.show()
                    binding.reply.tvReplyContent.text =
                        context.resources.getString(R.string.pinned_file) + message.message_reply?.files?.get(
                            0
                        )?.name_file?.replace("%20", " ")
                    val mineType =
                        message.message_reply?.files?.get(0)?.link_original?.let { getMimeType(it) }
                    ChatUtils.setImageFile(binding.reply.imgFile, mineType)
                }
                TechResEnumChat.TYPE_LINK.toString() -> {
                    binding.reply.rlReplyThumbContainer.show()
                    binding.reply.cvMediaReply.show()
                    binding.reply.imgReplyPlay.hide()
                    binding.reply.imgReplyAudio.hide()
                    binding.reply.imgFile.hide()
                    binding.reply.tvReplyContent.text =
                        String.format(
                            "%s %s",
                            context.resources.getString(R.string.pinned_link),
                            message.message_reply?.message_link?.url ?: ""
                        )
                    showImage(
                        binding.reply.imgReply,
                        message.message_reply?.message_link?.media_thumb
                    )
                }
                TechResEnumChat.TYPE_BUSINESS_CARD.toString() -> {
                    binding.reply.rlReplyThumbContainer.show()
                    binding.reply.cvMediaReply.show()
                    binding.reply.imgReplyPlay.hide()
                    binding.reply.imgReplyAudio.hide()
                    binding.reply.imgFile.hide()
                    binding.reply.tvReplyContent.text =
                        String.format(
                            "%s %s",
                            context.resources.getString(R.string.pinned_business_card),
                            message.message_reply?.message_phone?.full_name ?: ""
                        )

                    Utils.getImage(
                        binding.reply.imgReply,
                        message.message_reply?.message_phone?.avatar?.medium,
                        configNodeJs
                    )
                }
            }
        } else {
            binding.reply.tvReplyContent.text = context.resources.getString(R.string.revoke_message)
            binding.reply.rlReplyThumbContainer.hide()
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
        ChatUtils.setViewTimeRight(binding.reaction.lnReaction, binding.lnReply)

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
        binding.lnReply.setOnClickListener {
            chatGroupHandler?.onScrollMessage(message)
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