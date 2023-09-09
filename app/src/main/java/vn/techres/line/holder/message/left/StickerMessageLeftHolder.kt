package vn.techres.line.holder.message.left

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemStickerChatLeftBinding
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.RevokeMessageHandler

class StickerMessageLeftHolder(private val binding: ItemStickerChatLeftBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        configNodeJs: ConfigNodeJs,
        position: Int,
        dataSource: ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?,
        revokeMessageHandler: RevokeMessageHandler?
    ) {
        val message = dataSource[position]

        binding.tvNameSticker.text = message.sender.full_name
        getImage(binding.imgAvatar, message.sender.avatar?.medium, configNodeJs)
//        binding.tvTimeSticker.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeSticker, position, dataSource)

        if (message.message_share != null && !StringUtils.isNullOrEmpty(message.message_share?._id)) {
            Glide.with(binding.imgSticker)
                .load(
                    ChatUtils.getUrl(
                        message.message_share?.files?.get(0)?.link_original,
                        configNodeJs
                    )
                )
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_sticker)
                .error(R.drawable.ic_sticker)
                .into(binding.imgSticker)
        } else {
            Glide.with(binding.imgSticker)
                .load(ChatUtils.getUrl(message.files[0].link_original, configNodeJs))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_sticker)
                .error(R.drawable.ic_sticker)
                .into(binding.imgSticker)
        }
        if (position >= 0 && (position + 1) < dataSource.size) {
            if (dataSource[position].sender.member_id == dataSource[position + 1].sender.member_id && dataSource[position + 1].message_type.equals(
                    TechResEnumChat.TYPE_STICKER.toString()
                )
            ) {
                binding.tvNameSticker.hide()
                binding.cvSticker.visibility = View.INVISIBLE
            } else {
                binding.tvNameSticker.show()
                binding.cvSticker.show()
            }
        }
        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()

        if (message.reactions.reactions_count == 0) {
            binding.lnReactionContainer.hide()
        } else {
            ChatUtils.setReaction(
                binding.lnReactionContainer,
                binding.reaction,
                message.reactions
            )
        }

        ChatUtils.setViewTimeLeft(binding.reaction.lnReaction, binding.imgSticker, 4f)

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