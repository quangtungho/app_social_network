package vn.techres.line.holder.luckywheel

import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.game.luckywheel.MessageGameLuckyWheel
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemStickerMessageLuckyWheelBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.getAvatarCircle
import vn.techres.line.helper.Utils.getGlide
import vn.techres.line.interfaces.MessageLuckyWheelHandler

class StickerMessageLuckyWheel(val binding : ItemStickerMessageLuckyWheelBinding) : RecyclerView.ViewHolder(binding.root){
    fun bind(configNodeJs: ConfigNodeJs, messageGameLuckyWheel: MessageGameLuckyWheel, messageLuckyWheelHandler : MessageLuckyWheelHandler?){
        binding.tvNameSticker.text = messageGameLuckyWheel.sender?.full_name ?: ""
        getAvatarCircle(binding.imgAvatar, messageGameLuckyWheel.sender?.avatar?.medium, configNodeJs)
        getGlide(binding.imgSticker, messageGameLuckyWheel.message, configNodeJs)
        binding.tvTimeSticker.text = TimeFormatHelper.getDateFromStringDay(messageGameLuckyWheel.created_at)
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