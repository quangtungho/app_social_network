package vn.techres.line.holder.luckywheel

import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.game.luckywheel.MessageGameLuckyWheel
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemTextMessageLuckyWheelBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.interfaces.MessageLuckyWheelHandler

class TextMessageLuckyWheel(val binding : ItemTextMessageLuckyWheelBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(configNodeJs: ConfigNodeJs, messageGameLuckyWheel: MessageGameLuckyWheel, messageLuckyWheelHandler : MessageLuckyWheelHandler?){
        binding.tvNameText.text = messageGameLuckyWheel.sender?.full_name ?: ""
        Utils.getAvatarCircle(
            binding.imgAvatar,
            messageGameLuckyWheel.sender?.avatar?.medium,
            configNodeJs
        )
        binding.tvMessage.text = messageGameLuckyWheel.message
        binding.tvTimeText.text = TimeFormatHelper.getDateFromStringDay(messageGameLuckyWheel.created_at)
//        if (messageGameLuckyWheel.reactions.reactions_count == 0) {
//            binding.lnReactionContainer.hide()
//        } else {
//            ChatUtils.setReaction(
//                binding.lnReactionContainer,
//                binding.reaction,
//                messageGameLuckyWheel.reactions
//            )
//        }
//        binding.reaction.lnReactionPress.setOnClickListener {
//            messageLuckyWheelHandler?.onReaction(messageGameLuckyWheel)
//        }
    }
}