package vn.techres.line.holder.message.left

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.databinding.ItemGiphyChatLeftBinding
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.interfaces.chat.RevokeMessageHandler
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.helper.techresenum.TechResEnumChat

class GiPhyMessageLeftHolder(private val binding: ItemGiphyChatLeftBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        configNodeJs: ConfigNodeJs,
        dataSource : ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?,
        revokeMessageHandler: RevokeMessageHandler?
    ) {
        val message = dataSource[bindingAdapterPosition]
        binding.gifView.setMedia(message.giphy)
        binding.tvNameGiPhy.text = message.sender.full_name
        Utils.getImage(binding.imgAvatar, message.sender.avatar?.medium, configNodeJs)
        binding.tvTimeGiPhy.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        if (bindingAdapterPosition >= 0 && (bindingAdapterPosition + 1) < dataSource.size) {
            if (dataSource[bindingAdapterPosition].sender.member_id == dataSource[bindingAdapterPosition + 1].sender.member_id
                && dataSource[bindingAdapterPosition + 1].message_type.equals(
                    TechResEnumChat.TYPE_GI_PHY.toString()
                ) ) {
                binding.tvNameGiPhy.visibility = View.GONE
                binding.cvGiPhy.visibility = View.INVISIBLE
            } else {
                binding.tvNameGiPhy.visibility = View.VISIBLE
                binding.cvGiPhy.visibility = View.VISIBLE
            }
        }

        if (message.today == 1) {
            binding.tvTimeHeader.visibility = View.VISIBLE
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.visibility = View.GONE

        if (message.reactions.reactions_count == 0) {
            binding.reaction.lnReaction.visibility = View.GONE
            binding.reaction.imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
            binding.reaction.imgReactionPress.visibility = View.VISIBLE
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
            revokeMessageHandler?.onRevoke(
                message,
                it
            )
            true
        }
    }
}