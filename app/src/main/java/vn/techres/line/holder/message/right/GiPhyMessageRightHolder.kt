package vn.techres.line.holder.message.right

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.databinding.ItemGiphyChatRightBinding
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.interfaces.chat.RevokeMessageHandler
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.data.model.chat.MessagesByGroup

class GiPhyMessageRightHolder(private val binding : ItemGiphyChatRightBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: MessagesByGroup,
             chatGroupHandler: ChatGroupHandler?,
             revokeMessageHandler: RevokeMessageHandler?){

        binding.gifView.setMedia(message.giphy)
        binding.tvTimeGiPhy.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
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